package com.nexashop.api.service;

import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.domain.tenant.entity.Tenant;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import com.nexashop.api.util.UploadUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductImageBackgroundRemovalService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageBackgroundRemovalService.class);
    private static final String PART_NAME = "file";
    private static final String CRLF = "\r\n";
    private static final String NO_PLATFORM_WATERMARK_FEATURE_CODE = "NO_PLATFORM_WATERMARK";
    private static final String CUSTOM_WATERMARK_FEATURE_CODE = "CUSTOM_WATERMARK";
    private static final String PLATFORM_WATERMARK_TEXT_CONFIG_KEY = "PLATFORM_WATERMARK_TEXT";
    private static final String PLATFORM_WATERMARK_ENABLED_CONFIG_KEY = "PLATFORM_WATERMARK_ENABLED";
    private static final String PLATFORM_WATERMARK_KIND_CONFIG_KEY = "PLATFORM_WATERMARK_KIND";
    private static final String PLATFORM_WATERMARK_LOGO_URL_CONFIG_KEY = "PLATFORM_WATERMARK_LOGO_URL";
    private static final String WATERMARK_KIND_TEXT = "TEXT";
    private static final String WATERMARK_KIND_IMAGE = "IMAGE";
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );

    private final boolean enabled;
    private final URI rembgUri;
    private final Duration timeout;
    private final HttpClient httpClient;
    private final CurrentUserProvider currentUserProvider;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final PlanFeatureRepository planFeatureRepository;
    private final PlatformConfigRepository platformConfigRepository;
    private final PremiumFeatureRepository featureRepository;
    private final TenantRepository tenantRepository;
    private final Path uploadBaseDir;
    private final boolean platformWatermarkEnabled;
    private final String platformWatermarkText;

    public ProductImageBackgroundRemovalService(
            @Value("${image.bg-removal.enabled:false}") boolean enabled,
            @Value("${image.bg-removal.rembg-url:http://localhost:5000}") String rembgBaseUrl,
            @Value("${image.bg-removal.rembg-endpoint:/api/remove}") String rembgEndpoint,
            @Value("${image.bg-removal.timeout-seconds:30}") long timeoutSeconds,
            @Value("${image.bg-removal.platform-watermark-enabled:true}") boolean platformWatermarkEnabled,
            @Value("${image.bg-removal.platform-watermark-text:Digimart}") String platformWatermarkText,
            @Value("${app.upload.dir:}") String uploadBaseDir,
            CurrentUserProvider currentUserProvider,
            TenantSubscriptionRepository subscriptionRepository,
            PlanFeatureRepository planFeatureRepository,
            PlatformConfigRepository platformConfigRepository,
            PremiumFeatureRepository featureRepository,
            TenantRepository tenantRepository
    ) {
        this.enabled = enabled;
        this.rembgUri = resolveRembgUri(rembgBaseUrl, rembgEndpoint);
        long safeTimeoutSeconds = Math.max(5L, timeoutSeconds);
        this.timeout = Duration.ofSeconds(safeTimeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(10L, safeTimeoutSeconds)))
                .build();
        this.platformWatermarkEnabled = platformWatermarkEnabled;
        this.platformWatermarkText = platformWatermarkText == null ? "" : platformWatermarkText.trim();
        this.currentUserProvider = currentUserProvider;
        this.subscriptionRepository = subscriptionRepository;
        this.planFeatureRepository = planFeatureRepository;
        this.platformConfigRepository = platformConfigRepository;
        this.featureRepository = featureRepository;
        this.tenantRepository = tenantRepository;
        this.uploadBaseDir = UploadUtil.resolveBaseDir(uploadBaseDir);
    }

    public ProcessedImage removeBackground(MultipartFile file) throws IOException {
        ProcessedImage processed = removeBackgroundRaw(file);
        WatermarkPayload payload = resolveAutoPlatformWatermarkPayload();
        if (payload != null) {
            return applyWatermark(processed.bytes(), processed.contentType(), payload);
        }
        return processed;
    }

    public ProcessedImage changeBackground(MultipartFile file, MultipartFile backgroundFile, BackgroundFit fit) throws IOException {
        ProcessedImage foreground = removeBackgroundRaw(file);
        BufferedImage background = resolveBackgroundImage(backgroundFile);
        ProcessedImage composed = composeWithBackground(
                foreground.bytes(),
                foreground.contentType(),
                background,
                fit == null ? BackgroundFit.COVER : fit
        );
        WatermarkPayload payload = resolveAutoPlatformWatermarkPayload();
        if (payload != null) {
            return applyWatermark(composed.bytes(), composed.contentType(), payload);
        }
        return composed;
    }

    public ProcessedImage addWatermark(MultipartFile file, WatermarkMode mode) throws IOException {
        UploadUtil.validateImage(file);
        byte[] sourceBytes = file.getBytes();
        String inputType = normalizeContentType(file.getContentType()).orElse(null);
        String detectedType = detectContentType(sourceBytes);
        String resolvedType = firstSupportedType(inputType, detectedType);
        String contentType = resolvedType == null ? "image/png" : resolvedType;

        WatermarkMode resolvedMode = mode == null ? WatermarkMode.AUTO : mode;
        WatermarkPayload payload = resolveRequestedWatermarkPayload(resolvedMode);
        return applyWatermark(sourceBytes, contentType, payload);
    }

    private BufferedImage resolveBackgroundImage(MultipartFile backgroundFile) throws IOException {
        if (backgroundFile != null && !backgroundFile.isEmpty()) {
            UploadUtil.validateImage(backgroundFile);
            BufferedImage uploaded = ImageIO.read(new ByteArrayInputStream(backgroundFile.getBytes()));
            if (uploaded == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded background image is invalid");
            }
            return uploaded;
        }

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null || currentUser.tenantId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tenant context is required to use the configured studio background"
            );
        }
        Tenant tenant = tenantRepository.findById(currentUser.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
        String backgroundUrl = Optional.ofNullable(tenant.getStudioBackgroundUrl()).orElse("").trim();
        if (backgroundUrl.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No studio background configured for this tenant. Upload one in tenant settings."
            );
        }
        BufferedImage studioBackground = loadManagedUploadImage(backgroundUrl);
        if (studioBackground == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Configured studio background file is missing or invalid"
            );
        }
        return studioBackground;
    }

    private ProcessedImage composeWithBackground(
            byte[] foregroundBytes,
            String fallbackContentType,
            BufferedImage background,
            BackgroundFit fit
    ) {
        try {
            BufferedImage foreground = ImageIO.read(new ByteArrayInputStream(foregroundBytes));
            if (foreground == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Background removal output is invalid");
            }

            int canvasWidth = background.getWidth();
            int canvasHeight = background.getHeight();
            if (canvasWidth <= 0 || canvasHeight <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Background image dimensions are invalid");
            }

            BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(background, 0, 0, canvasWidth, canvasHeight, null);

            DrawSpec drawSpec = computeDrawSpec(
                    foreground.getWidth(),
                    foreground.getHeight(),
                    canvasWidth,
                    canvasHeight,
                    fit == null ? BackgroundFit.COVER : fit
            );
            g2d.drawImage(foreground, drawSpec.x(), drawSpec.y(), drawSpec.width(), drawSpec.height(), null);
            g2d.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", output);
            return new ProcessedImage(output.toByteArray(), "image/png");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Unable to compose foreground with background, returning original image", ex);
            return new ProcessedImage(foregroundBytes, fallbackContentType == null ? "image/png" : fallbackContentType);
        }
    }

    private DrawSpec computeDrawSpec(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight,
            BackgroundFit fit
    ) {
        float widthRatio = (float) targetWidth / Math.max(1, sourceWidth);
        float heightRatio = (float) targetHeight / Math.max(1, sourceHeight);
        float scale = fit == BackgroundFit.CONTAIN ? Math.min(widthRatio, heightRatio) : Math.max(widthRatio, heightRatio);

        int drawWidth = Math.max(1, Math.round(sourceWidth * scale));
        int drawHeight = Math.max(1, Math.round(sourceHeight * scale));
        int x = Math.round((targetWidth - drawWidth) / 2f);
        int y = Math.round((targetHeight - drawHeight) / 2f);
        return new DrawSpec(x, y, drawWidth, drawHeight);
    }

    private ProcessedImage removeBackgroundRaw(MultipartFile file) throws IOException {
        if (!enabled) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Background removal is disabled. Set image.bg-removal.enabled=true"
            );
        }

        UploadUtil.validateImage(file);
        byte[] sourceBytes = file.getBytes();
        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .filter((name) -> !name.isBlank())
                .orElse("product-image");
        String inputType = normalizeContentType(file.getContentType()).orElse(null);

        MultipartPayload multipartPayload = buildMultipartPayload(sourceBytes, originalName, inputType);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(rembgUri)
                .timeout(timeout)
                .header("Content-Type", "multipart/form-data; boundary=" + multipartPayload.boundary())
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartPayload.body()))
                .build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Background removal interrupted", ex);
        } catch (IOException ex) {
            log.warn("Unable to reach rembg service at {}", rembgUri, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Background removal service is unreachable",
                    ex
            );
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String payload = new String(response.body(), StandardCharsets.UTF_8);
            String detail = payload.isBlank()
                    ? ("HTTP " + response.statusCode())
                    : ("HTTP " + response.statusCode() + " - " + truncate(payload));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Background removal failed: " + detail);
        }

        byte[] processedBytes = response.body();
        if (processedBytes == null || processedBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Background removal returned an empty image");
        }

        String headerType = normalizeContentType(response.headers().firstValue("Content-Type").orElse(null)).orElse(null);
        String detectedType = detectContentType(processedBytes);
        String resolvedType = firstSupportedType(headerType, detectedType, inputType);
        String contentType = resolvedType == null ? "image/png" : resolvedType;
        return new ProcessedImage(processedBytes, contentType);
    }

    private MultipartPayload buildMultipartPayload(byte[] fileBytes, String filename, String contentType) throws IOException {
        String safeFilename = filename.replace("\"", "_");
        String safeContentType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        String boundary = "------------------------" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + PART_NAME + "\"; filename=\"" + safeFilename + "\"" + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + safeContentType + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(fileBytes);
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));
        output.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));

        return new MultipartPayload(boundary, output.toByteArray());
    }

    private URI resolveRembgUri(String baseUrl, String endpoint) {
        String safeBase = (baseUrl == null || baseUrl.isBlank())
                ? "http://localhost:5000"
                : baseUrl.trim();
        String safeEndpoint = (endpoint == null || endpoint.isBlank())
                ? "/api/remove"
                : endpoint.trim();

        if (!safeEndpoint.startsWith("/")) {
            safeEndpoint = "/" + safeEndpoint;
        }
        if (safeBase.endsWith("/")) {
            safeBase = safeBase.substring(0, safeBase.length() - 1);
        }

        return URI.create(safeBase + safeEndpoint);
    }

    private Optional<String> normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return Optional.empty();
        }
        String normalized = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(normalized);
    }

    private String detectContentType(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 12
                && bytes[0] == 0x52
                && bytes[1] == 0x49
                && bytes[2] == 0x46
                && bytes[3] == 0x46
                && bytes[8] == 0x57
                && bytes[9] == 0x45
                && bytes[10] == 0x42
                && bytes[11] == 0x50) {
            return "image/webp";
        }
        if (bytes.length >= 6) {
            String signature = new String(bytes, 0, 6, StandardCharsets.US_ASCII);
            if ("GIF87a".equals(signature) || "GIF89a".equals(signature)) {
                return "image/gif";
            }
        }
        return null;
    }

    private String firstSupportedType(String... types) {
        for (String candidate : types) {
            if (candidate != null && ALLOWED_IMAGE_TYPES.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String truncate(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.length() <= 220) {
            return trimmed;
        }
        return trimmed.substring(0, 220) + "...";
    }

    private boolean shouldApplyPlatformWatermark() {
        if (!isPlatformWatermarkEnabled()) {
            return false;
        }
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null || currentUser.tenantId() == null) {
            return true;
        }
        return !hasFeatureEnabled(currentUser.tenantId(), NO_PLATFORM_WATERMARK_FEATURE_CODE);
    }

    private boolean hasFeatureEnabled(Long tenantId, String featureCode) {
        TenantSubscription subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseGet(() -> subscriptionRepository
                        .findByTenantIdAndStatus(tenantId, SubscriptionStatus.PENDING_ACTIVATION)
                        .orElse(null));
        if (subscription == null || subscription.getPlanId() == null) {
            return false;
        }
        List<PlanFeature> planFeatures = planFeatureRepository.findByPlanId(subscription.getPlanId());
        for (PlanFeature planFeature : planFeatures) {
            if (planFeature == null || planFeature.getFeatureId() == null) {
                continue;
            }
            PremiumFeature feature = featureRepository.findById(planFeature.getFeatureId()).orElse(null);
            if (feature == null || feature.getCode() == null) {
                continue;
            }
            if (featureCode.equalsIgnoreCase(feature.getCode()) && feature.isActive()) {
                return true;
            }
        }
        return false;
    }

    private WatermarkPayload resolveRequestedWatermarkPayload(WatermarkMode mode) {
        WatermarkMode resolvedMode = mode == null ? WatermarkMode.AUTO : mode;
        return switch (resolvedMode) {
            case PLATFORM -> resolvePlatformWatermarkPayloadOrThrow();
            case CUSTOM -> resolveCustomWatermarkPayloadOrThrow();
            case AUTO -> {
                WatermarkPayload custom = resolveCustomWatermarkPayload();
                if (custom != null) {
                    yield custom;
                }
                yield resolvePlatformWatermarkPayloadOrThrow();
            }
        };
    }

    private WatermarkPayload resolveAutoPlatformWatermarkPayload() {
        if (!shouldApplyPlatformWatermark()) {
            return null;
        }
        return resolvePlatformWatermarkPayloadOrNull();
    }

    private WatermarkPayload resolvePlatformWatermarkPayloadOrThrow() {
        if (!isPlatformWatermarkEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Platform watermark is disabled");
        }
        WatermarkPayload payload = resolvePlatformWatermarkPayloadOrNull();
        if (payload != null) {
            return payload;
        }
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Platform watermark is not configured (missing text/logo)"
        );
    }

    private WatermarkPayload resolveCustomWatermarkPayloadOrThrow() {
        WatermarkPayload payload = resolveCustomWatermarkPayload();
        if (payload != null) {
            return payload;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Custom watermark is unavailable for this tenant plan"
        );
    }

    private WatermarkPayload resolveCustomWatermarkPayload() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser != null
                && currentUser.tenantId() != null
                && hasFeatureEnabled(currentUser.tenantId(), CUSTOM_WATERMARK_FEATURE_CODE)) {
            Tenant tenant = tenantRepository.findById(currentUser.tenantId()).orElse(null);
            if (tenant != null) {
                BufferedImage tenantLogo = loadManagedUploadImage(tenant.getLogoUrl());
                if (tenantLogo != null) {
                    return WatermarkPayload.image(tenantLogo);
                }
            }
            String tenantName = tenant == null ? "" : Optional.ofNullable(tenant.getName()).orElse("").trim();
            if (!tenantName.isBlank()) {
                return WatermarkPayload.text(tenantName);
            }
        }
        return null;
    }

    private WatermarkPayload resolvePlatformWatermarkPayloadOrNull() {
        String kind = resolveConfiguredPlatformWatermarkKind();
        if (WATERMARK_KIND_IMAGE.equals(kind)) {
            BufferedImage logo = resolveConfiguredPlatformWatermarkLogo();
            if (logo != null) {
                return WatermarkPayload.image(logo);
            }
            String textFallback = resolveConfiguredPlatformWatermarkText();
            return textFallback.isBlank() ? null : WatermarkPayload.text(textFallback);
        }
        String text = resolveConfiguredPlatformWatermarkText();
        if (!text.isBlank()) {
            return WatermarkPayload.text(text);
        }
        BufferedImage logoFallback = resolveConfiguredPlatformWatermarkLogo();
        if (logoFallback != null) {
            return WatermarkPayload.image(logoFallback);
        }
        return null;
    }

    private boolean isPlatformWatermarkEnabled() {
        String configuredValue = platformConfigRepository.findByConfigKey(PLATFORM_WATERMARK_ENABLED_CONFIG_KEY)
                .map(PlatformConfig::getConfigValue)
                .map(String::trim)
                .orElse(null);
        if (configuredValue == null || configuredValue.isBlank()) {
            return platformWatermarkEnabled;
        }
        String normalized = configuredValue.toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized) || "off".equals(normalized)) {
            return false;
        }
        return platformWatermarkEnabled;
    }

    private String resolveConfiguredPlatformWatermarkKind() {
        String kind = platformConfigRepository.findByConfigKey(PLATFORM_WATERMARK_KIND_CONFIG_KEY)
                .map(PlatformConfig::getConfigValue)
                .map(String::trim)
                .orElse("");
        if (kind.isBlank()) {
            return WATERMARK_KIND_TEXT;
        }
        String normalized = kind.toUpperCase(Locale.ROOT);
        if (WATERMARK_KIND_IMAGE.equals(normalized)) {
            return WATERMARK_KIND_IMAGE;
        }
        return WATERMARK_KIND_TEXT;
    }

    private BufferedImage resolveConfiguredPlatformWatermarkLogo() {
        String logoUrl = platformConfigRepository.findByConfigKey(PLATFORM_WATERMARK_LOGO_URL_CONFIG_KEY)
                .map(PlatformConfig::getConfigValue)
                .map(String::trim)
                .orElse("");
        return loadManagedUploadImage(logoUrl);
    }

    private BufferedImage loadManagedUploadImage(String logoUrl) {
        String safeUrl = logoUrl == null ? "" : logoUrl.trim();
        if (safeUrl.isBlank()) {
            return null;
        }

        String relativePath;
        if (safeUrl.startsWith("/uploads/")) {
            relativePath = safeUrl.substring("/uploads/".length());
        } else if (safeUrl.startsWith("uploads/")) {
            relativePath = safeUrl.substring("uploads/".length());
        } else {
            return null;
        }
        if (relativePath.isBlank()) {
            return null;
        }

        Path resolved = uploadBaseDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(uploadBaseDir)) {
            return null;
        }
        if (!Files.exists(resolved)) {
            return null;
        }

        try (InputStream in = Files.newInputStream(resolved)) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            log.warn("Unable to load managed upload image from {}", safeUrl, ex);
            return null;
        }
    }

    private String resolveConfiguredPlatformWatermarkText() {
        String configured = platformConfigRepository.findByConfigKey(PLATFORM_WATERMARK_TEXT_CONFIG_KEY)
                .map(PlatformConfig::getConfigValue)
                .map(String::trim)
                .orElse("");
        if (!configured.isBlank()) {
            return configured;
        }
        return platformWatermarkText == null ? "" : platformWatermarkText.trim();
    }

    private ProcessedImage applyWatermark(byte[] bytes, String fallbackContentType, WatermarkPayload watermarkPayload) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) {
                log.warn("Unable to decode image for watermark, returning original image");
                return new ProcessedImage(bytes, fallbackContentType);
            }

            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawImage(source, 0, 0, null);

            if (watermarkPayload.logo() != null) {
                drawLogoWatermark(g2d, width, height, watermarkPayload.logo());
            } else if (watermarkPayload.text() != null && !watermarkPayload.text().isBlank()) {
                drawTextWatermark(g2d, width, height, watermarkPayload.text());
            }
            g2d.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", output);
            return new ProcessedImage(output.toByteArray(), "image/png");
        } catch (Exception ex) {
            log.warn("Unable to apply watermark, returning original image", ex);
            return new ProcessedImage(bytes, fallbackContentType);
        }
    }

    private void drawTextWatermark(Graphics2D g2d, int width, int height, String watermarkText) {
        int base = Math.max(1, Math.min(width, height));
        int fontSize = Math.max(12, Math.round(base * 0.06f));
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
        g2d.setFont(font);

        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(watermarkText);

        int edgePaddingX = Math.max(8, Math.round(base * 0.02f));
        int edgePaddingY = Math.max(8, Math.round(base * 0.02f));
        int cropSafeInsetX = Math.max(edgePaddingX, Math.round(width * 0.18f));
        int cropSafeInsetY = Math.max(edgePaddingY, Math.round(height * 0.18f));

        int preferredX = width - cropSafeInsetX - textWidth;
        int minX = edgePaddingX;
        int maxX = Math.max(minX, width - edgePaddingX - textWidth);
        int x = Math.max(minX, Math.min(preferredX, maxX));

        int preferredY = height - cropSafeInsetY - metrics.getDescent();
        int minY = metrics.getAscent() + edgePaddingY;
        int maxY = Math.max(minY, height - edgePaddingY - metrics.getDescent());
        int y = Math.max(minY, Math.min(preferredY, maxY));

        int paddingX = Math.max(6, Math.round(base * 0.015f));
        int paddingY = Math.max(4, Math.round(base * 0.01f));
        int rectX = Math.max(0, x - paddingX);
        int rectY = Math.max(0, y - metrics.getAscent() - paddingY);
        int rectWidth = Math.min(width - rectX, textWidth + (paddingX * 2));
        int rectHeight = Math.min(height - rectY, metrics.getHeight() + (paddingY * 2));
        g2d.setColor(new Color(0, 0, 0, 118));
        g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, Math.max(8, paddingX), Math.max(8, paddingX));

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(watermarkText, x + 1, y + 1);
        g2d.setColor(new Color(255, 255, 255, 176));
        g2d.drawString(watermarkText, x, y);
    }

    private void drawLogoWatermark(Graphics2D g2d, int width, int height, BufferedImage logo) {
        int base = Math.max(1, Math.min(width, height));
        int edgePaddingX = Math.max(8, Math.round(base * 0.02f));
        int edgePaddingY = Math.max(8, Math.round(base * 0.02f));
        int cropSafeInsetX = Math.max(edgePaddingX, Math.round(width * 0.18f));
        int cropSafeInsetY = Math.max(edgePaddingY, Math.round(height * 0.18f));

        int maxWidth = Math.max(28, Math.round(width * 0.24f));
        int maxHeight = Math.max(28, Math.round(height * 0.24f));
        float scale = Math.min(
                (float) maxWidth / Math.max(1, logo.getWidth()),
                (float) maxHeight / Math.max(1, logo.getHeight())
        );
        int drawWidth = Math.max(24, Math.round(logo.getWidth() * scale));
        int drawHeight = Math.max(24, Math.round(logo.getHeight() * scale));

        int preferredX = width - cropSafeInsetX - drawWidth;
        int minX = edgePaddingX;
        int maxX = Math.max(minX, width - edgePaddingX - drawWidth);
        int x = Math.max(minX, Math.min(preferredX, maxX));

        int preferredY = height - cropSafeInsetY - drawHeight;
        int minY = edgePaddingY;
        int maxY = Math.max(minY, height - edgePaddingY - drawHeight);
        int y = Math.max(minY, Math.min(preferredY, maxY));

        int bgPadding = Math.max(4, Math.round(base * 0.008f));
        g2d.setColor(new Color(0, 0, 0, 92));
        g2d.fillRoundRect(
                x - bgPadding,
                y - bgPadding,
                drawWidth + (bgPadding * 2),
                drawHeight + (bgPadding * 2),
                Math.max(8, bgPadding * 2),
                Math.max(8, bgPadding * 2)
        );

        var previousComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2d.drawImage(logo, x, y, drawWidth, drawHeight, null);
        g2d.setComposite(previousComposite);
    }

    public enum BackgroundFit {
        COVER,
        CONTAIN;

        public static BackgroundFit from(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return COVER;
            }
            try {
                return BackgroundFit.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported background fit. Allowed: COVER, CONTAIN"
                );
            }
        }
    }

    public enum WatermarkMode {
        AUTO,
        PLATFORM,
        CUSTOM;

        public static WatermarkMode from(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return AUTO;
            }
            try {
                return WatermarkMode.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported watermark mode. Allowed: AUTO, PLATFORM, CUSTOM"
                );
            }
        }
    }

    private record MultipartPayload(String boundary, byte[] body) {
    }

    private record DrawSpec(int x, int y, int width, int height) {
    }

    private record WatermarkPayload(String text, BufferedImage logo) {
        private static WatermarkPayload text(String text) {
            return new WatermarkPayload(text, null);
        }

        private static WatermarkPayload image(BufferedImage logo) {
            return new WatermarkPayload(null, logo);
        }
    }

    public record ProcessedImage(byte[] bytes, String contentType) {
    }
}
