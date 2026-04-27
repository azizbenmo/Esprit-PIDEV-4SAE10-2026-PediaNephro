package com.nephroforum.service;

import com.nephroforum.dto.ChatbotDTOs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public ChatbotDTOs.ChatbotResponse answer(ChatbotDTOs.ChatbotRequest req) {
        return webClient.post()
                .uri("/chatbot")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ChatbotDTOs.ChatbotResponse.class)
                .block();
    }
}