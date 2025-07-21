package com.dataury.soloJ.domain.ai.controller;

import com.dataury.soloJ.domain.ai.dto.AiRequestDto;
import com.dataury.soloJ.domain.ai.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/chatGpt")
@RequiredArgsConstructor
public class AiController {

    private final ChatGPTService chatGPTService;

    @PostMapping("/api/chatGPT")
    public ResponseEntity<String> chatGPT(@RequestBody AiRequestDto requestDto) {
        return chatGPTService.chat(requestDto);
    }
}
