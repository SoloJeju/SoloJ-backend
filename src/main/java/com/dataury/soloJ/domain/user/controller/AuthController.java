package com.dataury.soloJ.domain.user.controller;

import com.dataury.soloJ.domain.user.converter.AuthConverter;
import com.dataury.soloJ.domain.user.dto.AuthRequestDTO;
import com.dataury.soloJ.domain.user.dto.AuthResponseDTO;
import com.dataury.soloJ.domain.user.dto.UserRequestDto;
import com.dataury.soloJ.domain.user.dto.UserResponseDto;
import com.dataury.soloJ.domain.user.entity.status.Role;
import com.dataury.soloJ.domain.user.service.AuthService;
import com.dataury.soloJ.domain.user.service.MailService;
import com.dataury.soloJ.domain.user.service.UserService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final MailService mailService;
    private final UserService userService;


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

    @PostMapping("/reissue")
    @Operation(summary = "Access 토큰 재발급 API", description = "만료된 access 토큰을 새로 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH003", description = "access 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH004", description = "access 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH006", description = "access 토큰 모양이 이상함", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Map<String, String>> reissue(@RequestBody AuthRequestDTO.RefreshRequestDTO request) {
        return ApiResponse.onSuccess(authService.reissueAccessToken(request.getRefreshToken()));
    }


    @PostMapping("/kakao/profile")
    @Operation(summary = "카카오 회원가입", description = "카카오 로그인의 사용자의 프로필을 설정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH003", description = "access 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH004", description = "access 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH006", description = "access 토큰 모양이 이상함", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<AuthResponseDTO.SignResponseDTO> kakaoProfile( @RequestBody AuthRequestDTO.KakaoRequestDTO kakaoRequestDTO) {
        return ApiResponse.onSuccess(authService.setProfile(kakaoRequestDTO));
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복확인", description = "중복된 이메일이 있는지 확인합니다. ")
    public ApiResponse<String> checkEmail(@RequestParam String email) {
        authService.validateEmail(email);
        authService.duplicationCheckEmail(email);
        return ApiResponse.onSuccess("중복 없음");
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복확인", description = "중복된 닉네임이 있는지 확인합니다. ")
    public ApiResponse<String> checNickName(@RequestParam String nickName) {
        authService.duplicationCheckNickName(nickName);
        return ApiResponse.onSuccess("중복 없음");
    }

    @PostMapping("/send-email")
    @Operation(summary = "이메일 인증코드 전송", description = "이메일 인증코드를 전송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<String> mailSend(@RequestBody String email) {
        if (email == null || email.isEmpty()) {
            throw new GeneralException(ErrorStatus.EMAIL_NOT_FOUND);
        }
        mailService.sendMail(email);
        return ApiResponse.onSuccess("인증 코드가 이메일로 전송되었습니다.");
    }

    @GetMapping("/check-number")
    @Operation(summary = "이메일 인증코드 번호 체크", description = "전송된 이메일 인증코드와 번호를 체크합니다. ")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Boolean> numberCheck(@RequestParam String email, Integer number) {

        if (mailService.checkVerificationNumber(email, number))
            return ApiResponse.onSuccess(true);

        return ApiResponse.onFailure("COMMON400", "이메일 인증번호와 다릅니다.", false);
    }

    @GetMapping("/validate-password")
    @Operation(summary = "비밀번호 유효성 체크", description = "비밀번호 유효성을 체크합니다. ")
    public ApiResponse<String> validatePassword(@RequestParam String password) {
        authService.validatePassword(password);
        return ApiResponse.onSuccess("비밀번호가 유효합니다");
    }

    @PatchMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다. 사용자 이메일과 비밀번호를 주세요.")
    public ApiResponse<String> changePassword(@RequestParam String email, String password) {
        authService.changePassword(email,password);
        return ApiResponse.onSuccess("비밀번호가 변경되었습니다.");
    }

}
