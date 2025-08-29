package com.dataury.soloJ.domain.user.service;

import com.dataury.soloJ.domain.user.dto.UserRequestDto;
import com.dataury.soloJ.domain.user.dto.UserResponseDto;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // 내 정보 조회
    public UserResponseDto.MyInfoDto getMyInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
        
        return UserResponseDto.MyInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickName(userProfile != null ? userProfile.getNickName() : null)
                .imageUrl(userProfile != null ? userProfile.getImageUrl() : null)
                .birth(userProfile != null ? userProfile.getBirthDate(): null)
                .gender(userProfile != null ? userProfile.getGender() : null)
                .country(userProfile != null ? userProfile.getCountry().name() : null)
                .soloPlanCount(user.getSoloPlanCount())
                .groupChatCount(user.getGroupChatCount())
                .build();
    }

    // 다른 사용자 프로필 조회
    public UserResponseDto.ProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        // 탈퇴한 사용자인 경우 탈퇴 정보만 반환
        if (!user.isActive()) {
            return UserResponseDto.ProfileDto.builder()
                    .userId(user.getId())
                    .nickName("탈퇴한 사용자입니다")
                    .imageUrl(null)
                    .birth(null)
                    .gender(null)
                    .country(null)
                    .soloPlanCount(0)
                    .groupChatCount(0)
                    .build();
        }
        
        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
        
        return UserResponseDto.ProfileDto.builder()
                .userId(user.getId())
                .nickName(userProfile != null ? userProfile.getNickName() : null)
                .imageUrl(userProfile != null ? userProfile.getImageUrl() : null)
                .birth(userProfile != null ? userProfile.getBirthDate() : null)
                .gender(userProfile != null ? userProfile.getGender() : null)
                .country(userProfile != null ? userProfile.getCountry().name() : null)
                .soloPlanCount(user.getSoloPlanCount())
                .groupChatCount(user.getGroupChatCount())
                .build();
    }

    // 프로필 수정
    @Transactional
    public UserResponseDto.MyInfoDto updateProfile(UserRequestDto.UpdateProfileDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_PROFILE_NOT_FOUND));
        
        // 프로필 업데이트 (null이 아닌 값들만 업데이트)
        userProfile.updateProfile(
                request.getNickName() != null ? request.getNickName() : userProfile.getNickName(),
                userProfile.getBirthDate(),
                userProfile.getGender(),
                userProfile.getUserType(),
                request.getImageName() != null ? request.getImageName() : userProfile.getImageName(),
                request.getImageUrl() != null ? request.getImageUrl() : userProfile.getImageUrl()
        );
        
        userProfileRepository.save(userProfile);
        
        return UserResponseDto.MyInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickName(userProfile.getNickName())
                .imageUrl(userProfile.getImageUrl())
                .birth(userProfile.getBirthDate())
                .gender(userProfile.getGender())
                .country(userProfile.getCountry().name())
                .soloPlanCount(user.getSoloPlanCount())
                .groupChatCount(user.getGroupChatCount())
                .build();
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        user.deactivate();
        userRepository.save(user);
    }
}