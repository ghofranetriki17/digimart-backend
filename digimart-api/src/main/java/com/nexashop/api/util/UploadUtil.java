package com.nexashop.api.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

public final class UploadUtil {

    private static final long MAX_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png",
            ".jpg",
            ".jpeg",
            ".webp",
            ".gif"
    );
    private static final Map<String, String> EXT_BY_TYPE = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private UploadUtil() {
    }

    public static StoredFile storeImage(MultipartFile file, String folder) throws IOException {
        return storeImage(file, "", folder);
    }

    public static StoredFile storeImage(MultipartFile file, String configuredBaseDir, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Image file is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(PAYLOAD_TOO_LARGE, "Image exceeds 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(BAD_REQUEST, "Only PNG, JPG, WEBP, or GIF images are allowed");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            extension = contentType != null ? EXT_BY_TYPE.get(contentType) : null;
        }
        if (extension == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Image file extension is not supported");
        }

        Path baseDir = resolveBaseDir(configuredBaseDir);
        Path uploadDir = baseDir.resolve(folder);
        Files.createDirectories(uploadDir);

        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());

        String relativeUrl = "/uploads/" + folder + "/" + filename;
        return new StoredFile(relativeUrl, filename, contentType, file.getSize());
    }

    public static Path resolveBaseDir(String configuredBaseDir) {
        if (StringUtils.hasText(configuredBaseDir)) {
            return Paths.get(configuredBaseDir).toAbsolutePath().normalize();
        }

        Path defaultDir = Paths.get("uploads").toAbsolutePath().normalize();
        if (Files.exists(defaultDir)) {
            return defaultDir;
        }

        Path fallbackDir = Paths.get("digimart-api", "uploads").toAbsolutePath().normalize();
        if (Files.exists(fallbackDir)) {
            return fallbackDir;
        }

        return defaultDir;
    }

    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    public record StoredFile(String relativeUrl, String filename, String contentType, long size) {
    }
}
