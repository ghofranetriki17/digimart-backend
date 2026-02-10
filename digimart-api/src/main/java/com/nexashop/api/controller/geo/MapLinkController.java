package com.nexashop.api.controller.geo;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapLinkController {

    private static final Pattern COORDS_PATTERN =
            Pattern.compile("(-?\\d+(?:\\.\\d+)?),\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern AT_PATTERN =
            Pattern.compile("@(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern ALT_PATTERN =
            Pattern.compile("!3d(-?\\d+(?:\\.\\d+)?)!4d(-?\\d+(?:\\.\\d+)?)");

    public record MapLinkResolveRequest(String url) {}

    public record MapLinkResolveResponse(String resolvedUrl, Double latitude, Double longitude) {}

    @PostMapping("/resolve")
    public ResponseEntity<MapLinkResolveResponse> resolve(@RequestBody MapLinkResolveRequest request) {
        if (request == null || request.url() == null || request.url().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String rawUrl = request.url().trim();
        try {
            URI inputUri = new URI(rawUrl);
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest httpRequest = HttpRequest.newBuilder(inputUri)
                    .GET()
                    .header("User-Agent", "Mozilla/5.0")
                    .build();
            HttpResponse<Void> response = client.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            URI resolved = response.uri();
            String resolvedUrl = resolved != null ? resolved.toString() : rawUrl;

            Optional<double[]> coords = parseLatLng(resolvedUrl);
            if (coords.isEmpty()) {
                coords = parseLatLng(rawUrl);
            }

            if (coords.isEmpty()) {
                return ResponseEntity.unprocessableEntity().build();
            }

            double[] values = coords.get();
            return ResponseEntity.ok(new MapLinkResolveResponse(resolvedUrl, values[0], values[1]));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Optional<double[]> parseLatLng(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        try {
            URI uri = new URI(input);
            String query = uri.getQuery();
            if (query != null) {
                for (String part : query.split("&")) {
                    int idx = part.indexOf('=');
                    if (idx < 0) continue;
                    String key = part.substring(0, idx);
                    String value = part.substring(idx + 1);
                    if ("q".equalsIgnoreCase(key) || "query".equalsIgnoreCase(key)) {
                        Optional<double[]> parsed = parseCoords(value);
                        if (parsed.isPresent()) {
                            return parsed;
                        }
                    }
                }
            }
        } catch (URISyntaxException ignored) {
            // ignore
        }

        Matcher atMatch = AT_PATTERN.matcher(input);
        if (atMatch.find()) {
            return parseCoords(atMatch.group(1) + "," + atMatch.group(2));
        }

        Matcher altMatch = ALT_PATTERN.matcher(input);
        if (altMatch.find()) {
            return parseCoords(altMatch.group(1) + "," + altMatch.group(2));
        }

        return parseCoords(input);
    }

    private Optional<double[]> parseCoords(String value) {
        if (value == null) {
            return Optional.empty();
        }
        Matcher matcher = COORDS_PATTERN.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            return Optional.of(new double[] { lat, lng });
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
