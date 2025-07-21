package com.dataury.soloJ.domain.ai.service;

import com.dataury.soloJ.domain.ai.dto.AiRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    @Value("${openai.secret-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ResponseEntity<String> chat(AiRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        JSONObject requestBody = new JSONObject();

        // chat 모델일 경우 (gpt-3.5-turbo, gpt-4 등)
        if (dto.getModel().startsWith("gpt-")) {
            JSONArray messages = new JSONArray();

            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", "너는 스프링 챗지피티 프로젝트 도우미야. 모든 답변은 간단한 자기소개 후에 해줘.");
            messages.add(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", dto.getPrompt());
            messages.add(user);

            requestBody.put("model", dto.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", dto.getTemperature());

            String endpoint = "https://api.openai.com/v1/chat/completions";
            return callOpenAiAPI(endpoint, requestBody, headers);
        }

        // 텍스트 생성 모델일 경우 (text-davinci-003 등)
        else {
            requestBody.put("model", dto.getModel());
            requestBody.put("prompt", dto.getPrompt());
            requestBody.put("temperature", dto.getTemperature());
            requestBody.put("max_tokens", 100);

            String endpoint = "https://api.openai.com/v1/completions";
            return callOpenAiAPI(endpoint, requestBody, headers);
        }
    }

    private ResponseEntity<String> callOpenAiAPI(String endpoint, JSONObject requestBody, HttpHeaders headers) {
        try {
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = new ObjectMapper().readTree(response.getBody());

                String result = root.path("choices").get(0).path("message").path("content").asText(null);
                if (result == null) {
                    result = root.path("choices").get(0).path("text").asText(); // completions 전용
                }

                return ResponseEntity.ok(result.trim());
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("OpenAI 호출 실패");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예외 발생: " + e.getMessage());
        }
    }
}
