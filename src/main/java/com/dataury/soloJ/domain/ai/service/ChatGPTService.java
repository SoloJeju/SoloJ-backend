package com.dataury.soloJ.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    @Value("${openai.secret-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    public String generate(String prompt) {
        try {
            String endpoint = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String requestBody = buildChatRequestBody(prompt);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                String result = root.path("choices").get(0).path("message").path("content").asText();

                return result.trim();
            } else {
                throw new RuntimeException("OpenAI 호출 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("OpenAI 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String buildChatRequestBody(String prompt) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", model);
        requestMap.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "너는 제주도 여행 전문 가이드야. 사용자 요청에 따라 여행 일정을 구성해줘."
        ));
        messages.add(Map.of(
                "role", "user",
                "content", prompt
        ));

        requestMap.put("messages", messages);

        return mapper.writeValueAsString(requestMap);
    }
}
