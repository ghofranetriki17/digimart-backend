package com.nexashop.application.usecase;

import com.nexashop.application.exception.BadRequestException;
import com.nexashop.application.port.out.AiTextProvider;
import com.nexashop.application.port.out.CurrentUserProvider;

public class AiTextUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AiTextProvider aiTextProvider;

    public AiTextUseCase(CurrentUserProvider currentUserProvider, AiTextProvider aiTextProvider) {
        this.currentUserProvider = currentUserProvider;
        this.aiTextProvider = aiTextProvider;
    }

    public String generateText(String prompt) {
        currentUserProvider.requireUser();
        if (prompt == null || prompt.isBlank()) {
            throw new BadRequestException("Prompt is required");
        }
        return aiTextProvider.generateText(prompt);
    }
}
