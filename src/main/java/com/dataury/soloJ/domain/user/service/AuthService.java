package com.dataury.soloJ.domain.user.service;


import com.dataury.soloJ.domain.user.converter.AuthConverter;
import com.dataury.soloJ.domain.user.dto.AuthRequestDTO;
import com.dataury.soloJ.domain.user.dto.AuthResponseDTO;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.entity.status.Role;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.TokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenProvider tokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public AuthResponseDTO.SignResponseDTO signUp(AuthConverter.UserRegistrationData data) {
        validatePassword(data.user().getPassword());
        validateEmail(data.user().getEmail());

        String encodedPassword = passwordEncoder.encode(data.user().getPassword());
        data.user().changePassword(encodedPassword);

        try {
            User savedUser = userRepository.save(data.user());
            UserProfile userProfile = data.userProfile();
            userProfile.setUser(savedUser);
            userProfileRepository.save(userProfile);

            return AuthConverter.toSigninResponseDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            String rootMsg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();

            if (rootMsg != null) {
                if (rootMsg.contains("uk_user_email")) {
                    throw new GeneralException(ErrorStatus.EMAIL_DUPLICATE);
                } else if (rootMsg.contains("uk_user_phone_number")) {
                    throw new GeneralException(ErrorStatus.PHONE_DUPLICATE);
                } else if (rootMsg.contains("uk_user_nick_name")) {
                    throw new GeneralException(ErrorStatus.NICKNAME_DUPLICATE);
                }

                throw new GeneralException(ErrorStatus.DATABASE_ERROR);
            }
        }
        return null;//도달하지 않는 코드
    }


    public void validatePassword(String password) {
        // 길이 검사
        if (password.length() < 8 || password.length() > 12) {
            throw new GeneralException(ErrorStatus.PASSWORD_VALIDATION_FAILED);
        }

        // 영어 대문자, 소문자, 숫자 포함 여부 검사
        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        // 최소 2종류 이상 포함 여부 확인
        int count = 0;
        if (hasUpperCase) count++;
        if (hasLowerCase) count++;
        if (hasDigit) count++;

        if (count < 2) {
            throw new GeneralException(ErrorStatus.PASSWORD_VALIDATION_FAILED);
        }
    }

    // 이메일 형식 검증
    public void validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        if (!email.matches(emailRegex))
            throw new GeneralException(ErrorStatus.EMAIL_VALIDATION_FAILED);

    }


}
