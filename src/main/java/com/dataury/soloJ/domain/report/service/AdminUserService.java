package com.dataury.soloJ.domain.report.service;

import com.dataury.soloJ.domain.report.dto.admin.ReportedUserDto;
import com.dataury.soloJ.domain.report.dto.admin.UserActionDto;
import com.dataury.soloJ.domain.report.entity.UserPenalty;
import com.dataury.soloJ.domain.report.entity.UserPenaltyHistory;
import com.dataury.soloJ.domain.report.repository.ReportRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyHistoryRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserPenaltyRepository userPenaltyRepository;
    private final UserPenaltyHistoryRepository historyRepository;
    private final ReportRepository reportRepository;

    public List<ReportedUserDto> getReportedUsers(int page, int limit, String status, String search) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        // Get users who have been reported
        List<Long> reportedUserIds = reportRepository.findDistinctTargetUserIds();
        

        List<User> reportedUsers = userRepository.findAllById(reportedUserIds);
        
        return reportedUsers.stream()
            .map(this::convertToReportedUserDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void applyUserAction(Long userId, UserActionDto actionDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        UserPenalty penalty = userPenaltyRepository.findByUserId(userId)
            .orElse(UserPenalty.builder()
                .userId(userId)
                .user(user)
                .reportCount(0)
                .penaltyLevel(0)
                .build());

        switch (actionDto.getActionType()) {
            case "warning":
                // Warning action
                break;
            case "softBlock":
                penalty.setPenaltyLevel(1);
                break;
            case "restrictWriting":
                penalty.setPenaltyLevel(2);
                penalty.setRestrictedUntil(LocalDateTime.now().plusDays(actionDto.getDuration()));
                break;
            case "permanentBan":
                penalty.setPenaltyLevel(3);
                user.deactivate();
                break;
            case "restore":
                penalty.setPenaltyLevel(0);
                penalty.setRestrictedUntil(null);
                user.activate();
                break;
        }

        userPenaltyRepository.save(penalty);

        // Save action history
        UserPenaltyHistory history = UserPenaltyHistory.builder()
            .userId(userId)
            .action(actionDto.getActionType())
            .build();
        historyRepository.save(history);
    }

    @Transactional
    public void updateUserStatus(Long userId, String status, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        switch (status) {
            case "active":
                user.activate();
                break;
            case "inactive":
                user.deactivate();
                break;
            case "restricted":
                // Handle restriction logic
                break;
        }

        userRepository.save(user);
    }

    private ReportedUserDto convertToReportedUserDto(User user) {
        UserPenalty penalty = userPenaltyRepository.findByUserId(user.getId()).orElse(null);
        
        String currentStatus = "normal";
        if (!user.isActive()) {
            currentStatus = "banned";
        } else if (penalty != null && penalty.getRestrictedUntil() != null && penalty.getRestrictedUntil().isAfter(LocalDateTime.now())) {
            currentStatus = "restricted";
        }

        return ReportedUserDto.builder()
            .userId(user.getId())
            .userName(user.getName())
            .totalReports(penalty != null ? penalty.getReportCount() : 0)
            .currentStatus(currentStatus)
            .recentReports(List.of()) // Would populate with recent reports
            .lastAction(null) // Would populate with last action
            .build();
    }
}