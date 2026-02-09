package com.nexashop.api.controller.ai;

import com.nexashop.api.dto.request.ai.AiTextRequest;
import com.nexashop.api.dto.response.ai.AiTextResponse;
import com.nexashop.application.usecase.AiTextUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiTextUseCase aiTextUseCase;

    public AiController(AiTextUseCase aiTextUseCase) {
        this.aiTextUseCase = aiTextUseCase;
    }

    @PostMapping("/text")
    public AiTextResponse generateText(@Valid @RequestBody AiTextRequest request) {
        String text = aiTextUseCase.generateText(request.getPrompt());
        return AiTextResponse.builder()
                .text(text)
                .build();
    }
}
