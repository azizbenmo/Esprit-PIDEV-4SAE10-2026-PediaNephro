package com.nephroforum.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

public class ChatbotDTOs {

    @Getter @Setter
    public static class ChatbotRequest {
        private String question;
        private PatientProfile profile;
        private List<MessageHistory> history = new ArrayList<>();
    }

    @Getter @Setter
    public static class MessageHistory {
        private String role;
        private String content;
    }

    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ChatbotResponse {
        private String answer;
        private boolean criticalCase;
        private String disclaimer;
        private String redirectMessage;
        private List<String> suggestedQuestions;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PatientProfile {
        private String patientName;
        private int age;
        private int ckdStage;
        private String currentTreatment;
        private List<String> allergies = new ArrayList<>();
    }
}