package com.nexashop.api.service;

import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.domain.tenant.entity.Tenant;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
    private final PremiumFeatureRepository featureRepository;
    private final TenantRepository tenantRepository;
    private final boolean platformWatermarkEnabled;
    private final String platformWatermarkText;

    public ProductImageBackgroundRemovalService(
            @Value("${image.bg-removal.enabled:false}") boolean enabled,
            @Value("${image.bg-removal.rembg-url:http://localhost:5000}") String rembgBaseUrl,
            @Value("${image.bg-removal.rembg-endpoint:/api/remove}") String rembgEndpoint,
            @Value("${image.bg-removal.timeout-seconds:30}") long timeoutSeconds,
            @Value("${image.bg-removal.platform-watermark-enabled:true}") boolean platformWatermarkEnabled,
            @Value("${image.bg-removal.platform-watermark-text:Digimart}") String platformWatermarkText,
            CurrentUserProvider currentUserProvider,
            TenantSubscriptionRepository subscriptionRepository,
            PlanFeatureRepository planFeatureRepository,
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
        this.featureRepository = featureRepository;
        this.tenantRepository = tenantRepository;
    }

    public ProcessedImage removeBackground(MultipartFile file) throws IOException {
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
        if (shouldApplyPlatformWatermark()) {
            String watermarkText = resolveWatermarkText();
            if (!watermarkText.isBlank()) {
                return applyPlatformWatermark(processedBytes, contentType, watermarkText);
            }
        }
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
        if (!platformWatermarkEnabled) {
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

    private String resolveWatermarkText() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser != null
                && currentUser.tenantId() != null
                && hasFeatureEnabled(currentUser.tenantId(), CUSTOM_WATERMARK_FEATURE_CODE)) {
            String tenantName = tenantRepository.findById(currentUser.tenantId())
                    .map(Tenant::getName)
                    .map(String::trim)
                    .orElse("");
            if (!tenantName.isBlank()) {
                return tenantName;
            }
        }
        return platformWatermarkText == null ? "" : platformWatermarkText.trim();
    }

    private ProcessedImage applyPlatformWatermark(byte[] bytes, String fallbackContentType, String watermarkText) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) {
                log.warn("Unable to decode image for platform watermark, returning original image");
                return new ProcessedImage(bytes, fallbackContentType);
            }

            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawImage(source, 0, 0, null);

            int base = Math.max(1, Math.min(width, height));
            int fontSize = Math.max(12, Math.round(base * 0.06f));
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
            g2d.setFont(font);

            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(watermarkText);
            int x = Math.max(0, (width - textWidth) / 2);
            int y = Math.max(metrics.getAscent(), (height + metrics.getAscent()) / 2);

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
            g2d.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", output);
            return new ProcessedImage(output.toByteArray(), "image/png");
        } catch (Exception ex) {
            log.warn("Unable to apply platform watermark, returning original image", ex);
            return new ProcessedImage(bytes, fallbackContentType);
        }
    }

    private record MultipartPayload(String boundary, byte[] body) {
    }

    public record ProcessedImage(byte[] bytes, String contentType) {
    }
}
