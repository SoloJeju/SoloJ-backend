package com.dataury.soloJ.domain.user.controller;

import com.dataury.soloJ.domain.user.converter.AuthConverter;
import com.dataury.soloJ.domain.user.dto.AuthRequestDTO;
import com.dataury.soloJ.domain.user.dto.AuthResponseDTO;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.entity.status.Role;
import com.dataury.soloJ.domain.user.service.AuthService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/adminSignup")
    @Operation(summary = "관리자 회원가입 API", description = "관리자 계정을 생성하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<AuthResponseDTO.SignResponseDTO> adminSignup(@RequestBody AuthRequestDTO.SignRequestDTO signRequestDTO) {
        AuthConverter.UserRegistrationData user = AuthConverter.toUser(signRequestDTO, Role.ADMIN);
        return ApiResponse.onSuccess(authService.signUp(user));
    }

    @PostMapping("/userSignup")
    @Operation(summary = "사용자 회원가입 API", description = "일반 사용자 계정을 생성하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<AuthResponseDTO.SignResponseDTO> userSignup(@RequestBody AuthRequestDTO.SignRequestDTO signRequestDTO) {

        AuthConverter.UserRegistrationData user = AuthConverter.toUser(signRequestDTO, Role.USER);
        return ApiResponse.onSuccess(authService.signUp(user));
    }

}
