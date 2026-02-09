package com.nexashop.infrastructure.ai;

import com.nexashop.application.exception.ExternalServiceException;
import com.nexashop.application.port.out.AiTextProvider;

public class DisabledAiTextProvider implements AiTextProvider {

    private final String message;

    public DisabledAiTextProvider(String message) {
        this.message = message == null || message.isBlank()
                ? "AI provider not configured"
                : message;
    }

    @Override
    public String generateText(String prompt) {
        throw new ExternalServiceException(message);
    }
}
