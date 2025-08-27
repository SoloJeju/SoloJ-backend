package com.dataury.soloJ.global.security;

import com.dataury.soloJ.domain.report.entity.UserPenalty;
import com.dataury.soloJ.domain.report.repository.UserPenaltyRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserPenaltyChecker {

    private final UserPenaltyRepository userPenaltyRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 현재 제재 상태를 확인합니다
     * @param userId 사용자 ID
     * @return PenaltyStatus
     */
    public PenaltyStatus checkUserPenalty(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 계정이 비활성화된 경우 (영구차단)
        if (!user.isActive()) {
            return PenaltyStatus.PERMANENT_BAN;
        }

        UserPenalty penalty = userPenaltyRepository.findByUserId(userId).orElse(null);
        if (penalty == null) {
            return PenaltyStatus.NORMAL;
        }

        LocalDateTime now = LocalDateTime.now();

        // 제재 기간이 만료된 경우
        if (penalty.getRestrictedUntil() != null && penalty.getRestrictedUntil().isBefore(now)) {
            return PenaltyStatus.NORMAL;
        }

        // penaltyLevel에 따른 제재 상태
        switch (penalty.getPenaltyLevel()) {
            case 1 -> {
                return PenaltyStatus.SOFT_BLOCK; // 일시 차단
            }
            case 2 -> {
                return PenaltyStatus.WRITING_RESTRICTION; // 작성 제한
            }
            case 3 -> {
                return PenaltyStatus.PERMANENT_BAN; // 영구 차단
            }
            default -> {
                return PenaltyStatus.NORMAL;
            }
        }
    }

    /**
     * 커뮤니티 글 작성 가능 여부 확인
     */
    public void checkCommunityWritePermission(Long userId) {
        PenaltyStatus status = checkUserPenalty(userId);
        
        switch (status) {
            case SOFT_BLOCK -> throw new GeneralException(ErrorStatus.USER_SOFT_BLOCKED);
            case WRITING_RESTRICTION -> throw new GeneralException(ErrorStatus.USER_WRITING_RESTRICTED);
            case PERMANENT_BAN -> throw new GeneralException(ErrorStatus.USER_PERMANENTLY_BANNED);
        }
    }

    /**
     * 동행방 참여/생성 가능 여부 확인
     */
    public void checkTravelPermission(Long userId) {
        PenaltyStatus status = checkUserPenalty(userId);
        
        switch (status) {
            case SOFT_BLOCK -> throw new GeneralException(ErrorStatus.USER_TRAVEL_RESTRICTED);
            case PERMANENT_BAN -> throw new GeneralException(ErrorStatus.USER_PERMANENTLY_BANNED);
            // WRITING_RESTRICTION은 동행방 참여 허용
        }
    }

    /**
     * 메시지 전송 가능 여부 확인
     */
    public void checkMessagePermission(Long userId) {
        PenaltyStatus status = checkUserPenalty(userId);
        
        switch (status) {
            case SOFT_BLOCK -> throw new GeneralException(ErrorStatus.USER_MESSAGE_RESTRICTED);
            case PERMANENT_BAN -> throw new GeneralException(ErrorStatus.USER_PERMANENTLY_BANNED);
            // WRITING_RESTRICTION은 메시지 전송 허용
        }
    }

    /**
     * 댓글 작성 가능 여부 확인
     */
    public void checkCommentPermission(Long userId) {
        checkCommunityWritePermission(userId); // 글 작성과 동일한 제재 적용
    }

    public enum PenaltyStatus {
        NORMAL,            // 정상
        SOFT_BLOCK,        // 일시 차단 (모든 활동 제한)
        WRITING_RESTRICTION, // 작성 제한 (글/댓글 작성만 제한)
        PERMANENT_BAN      // 영구 차단 (모든 활동 제한)
    }
}