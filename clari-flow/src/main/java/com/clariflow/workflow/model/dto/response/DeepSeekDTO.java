package com.clariflow.workflow.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek API request/response DTOs (OpenAI-compatible format).
 */
public class DeepSeekDTO {

    @Data
    public static class ChatRequest {
        private String model;
        private List<Message> messages;
        private double temperature;
        private int max_tokens;

        public ChatRequest() {}

        public ChatRequest(String model, List<Message> messages, double temperature, int maxTokens) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.max_tokens = maxTokens;
        }
    }

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatResponse {
        private String id;
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private int index;
        private Message message;
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
}
