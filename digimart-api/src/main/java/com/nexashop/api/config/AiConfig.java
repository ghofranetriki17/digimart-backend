package com.nexashop.api.config;

import com.nexashop.application.port.out.AiTextProvider;
import com.nexashop.infrastructure.ai.DisabledAiTextProvider;
import com.nexashop.infrastructure.ai.GeminiAiTextProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public AiTextProvider aiTextProvider(
            @Value("${ai.provider:gemini}") String provider,
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-2.5-flash}") String model,
            @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl
    ) {
        if (!"gemini".equalsIgnoreCase(provider)) {
            return new DisabledAiTextProvider("AI provider not configured");
        }
        String resolvedKey = apiKey == null || apiKey.isBlank()
                ? System.getenv("GEMINI_API_KEY")
                : apiKey;
        if (resolvedKey == null || resolvedKey.isBlank()) {
            return new DisabledAiTextProvider("Gemini API key is missing");
        }
        return new GeminiAiTextProvider(resolvedKey, model, baseUrl);
    }
}
