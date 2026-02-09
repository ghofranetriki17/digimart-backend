package com.nexashop.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexashop.application.exception.ExternalServiceException;
import com.nexashop.application.port.out.AiTextProvider;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiAiTextProvider implements AiTextProvider {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiAiTextProvider(String apiKey, String model, String baseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalServiceException("Gemini API key is missing");
        }
        this.apiKey = apiKey;
        this.model = model == null || model.isBlank() ? "gemini-2.5-flash" : model.trim();
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new ExternalServiceException("AI prompt is empty");
        }
        String endpoint = String.format("%s/v1beta/models/%s:generateContent", baseUrl, model);
        String payload = buildPayload(prompt);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(DEFAULT_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ExternalServiceException(buildErrorMessage(response));
            }
            return extractText(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("AI provider request was interrupted");
        } catch (IOException ex) {
            throw new ExternalServiceException("AI provider request failed: " + ex.getMessage());
        }
    }

    private String buildPayload(String prompt) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);
        try {
            return objectMapper.writeValueAsString(root);
        } catch (IOException ex) {
            throw new ExternalServiceException("AI request serialization failed");
        }
    }

    private String extractText(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new ExternalServiceException("AI provider returned empty response");
            }
            return textNode.asText().trim();
        } catch (IOException ex) {
            throw new ExternalServiceException("AI response parsing failed");
        }
    }

    private String buildErrorMessage(HttpResponse<String> response) {
        String baseMessage = "AI provider error (HTTP " + response.statusCode() + ")";
        try {
            JsonNode root = objectMapper.readTree(response.body());
            String message = root.path("error").path("message").asText();
            if (message != null && !message.isBlank()) {
                return baseMessage + ": " + message;
            }
        } catch (IOException ex) {
            // ignore parsing error
        }
        return baseMessage;
    }
}
