package com.chitnis.document_management_app.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public AiClient(
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.api.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${groq.api.model:llama-3.1-8b-instant}") String model
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ Groq API key is NOT configured!");
        } else {
            String maskedKey = apiKey.substring(0, Math.min(10, apiKey.length())) + "..." +
                              apiKey.substring(Math.max(apiKey.length() - 4, 0));
            log.info("✅ Groq API key loaded: {} | Base URL: {} | Model: {}", maskedKey, baseUrl, model);
        }
    }

    public String summarize(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Groq API key is not configured.");
        }

        String truncated = text.length() > 4000 ? text.substring(0, 4000) : text;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant that summarizes documents."),
                        Map.of("role", "user", "content", "Summarize the following document in 10 concise bullet points:\n\n" + truncated)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ChatCompletionResponse response;
        try {
            response = restTemplate.postForObject(
                    baseUrl + "/chat/completions",
                    request,
                    ChatCompletionResponse.class
            );
        } catch (RestClientResponseException ex) {
            String message = "AI provider error (" + ex.getRawStatusCode() + "): " + ex.getResponseBodyAsString();
            log.error("❌ Groq API call failed with status {}: {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            if (ex.getRawStatusCode() == 401 || ex.getRawStatusCode() == 403 || ex.getRawStatusCode() == 429) {
                log.error("❌ Authentication/Quota issue detected. Please check:");
                log.error("   1. Your API key is valid and not expired");
                log.error("   2. Your Groq account has billing enabled");
                log.error("   3. You haven't exceeded your quota");
                return fallbackSummary(truncated);
            }
            throw new IllegalStateException(message, ex);
        } catch (RestClientException ex) {
            log.error("❌ Failed to connect to Groq API: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            log.error("   Full error: ", ex);
            return fallbackSummary(truncated);
        }

        if (response == null || response.choices == null || response.choices.isEmpty()
                || response.choices.get(0).message == null
                || response.choices.get(0).message.content == null) {
            log.warn("Empty AI response received, using fallback summary.");
            return fallbackSummary(truncated);
        }

        return response.choices.get(0).message.content;
    }

    public String answerQuestion(String text, String question) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Groq API key is not configured.");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty.");
        }

        String truncated = text.length() > 6000 ? text.substring(0, 6000) : text;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String userPrompt = """
                You are given a document and a question.
                * Answer exclusively with facts stated in the document.
                * Quote or reference the specific detail that supports the answer.
                * If the document does not contain the information, reply exactly with: "The document does not mention this."
                
                Document:
                %s
                
                Question: %s
                Provide a concise answer plus a short supporting quote.
                """.formatted(truncated, question);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.1,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a careful analyst that answers strictly from the provided document."),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ChatCompletionResponse response;
        try {
            response = restTemplate.postForObject(
                    baseUrl + "/chat/completions",
                    request,
                    ChatCompletionResponse.class
            );
        } catch (RestClientResponseException ex) {
            String message = "AI provider error (" + ex.getRawStatusCode() + "): " + ex.getResponseBodyAsString();
            log.error("❌ Groq API call failed with status {}: {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            if (ex.getRawStatusCode() == 401 || ex.getRawStatusCode() == 403 || ex.getRawStatusCode() == 429) {
                log.error("❌ Authentication/Quota issue detected. Please check:");
                log.error("   1. Your API key is valid and not expired");
                log.error("   2. Your Groq account has billing enabled");
                log.error("   3. You haven't exceeded your quota");
                return "AI temporarily unavailable. Unable to answer the question.";
            }
            throw new IllegalStateException(message, ex);
        } catch (RestClientException ex) {
            log.error("❌ Failed to connect to Groq API: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            log.error("   Full error: ", ex);
            return "AI temporarily unavailable. Unable to answer the question.";
        }

        if (response == null || response.choices == null || response.choices.isEmpty()
                || response.choices.get(0).message == null
                || response.choices.get(0).message.content == null) {
            log.warn("Empty AI response received for Q&A.");
            return "AI did not return an answer.";
        }

        return response.choices.get(0).message.content.trim();
    }

    private String fallbackSummary(String text) {
        String[] paragraphs = text.split("\\r?\\n");
        StringBuilder builder = new StringBuilder("Fallback summary (AI unavailable):\n");
        int count = 0;
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            builder.append("- ").append(trimmed.length() > 180 ? trimmed.substring(0, 180) + "…" : trimmed).append("\n");
            if (++count == 10) {
                break;
            }
        }
        if (count == 0) {
            builder.append("- ").append(text.substring(0, Math.min(180, text.length())));
        }
        return builder.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatCompletionResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
