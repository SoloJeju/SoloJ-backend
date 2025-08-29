package com.dataury.soloJ.domain.inquiry.service;

import com.dataury.soloJ.domain.inquiry.dto.*;
import com.dataury.soloJ.domain.inquiry.entity.Inquiry;
import com.dataury.soloJ.domain.inquiry.entity.InquiryAttachment;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import com.dataury.soloJ.domain.inquiry.repository.InquiryAttachmentRepository;
import com.dataury.soloJ.domain.inquiry.repository.InquiryRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    // ===== 사용자용 메서드 =====

    @Transactional
    public InquiryResponseDto createInquiry(InquiryRequestDto requestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getCategory())
                .userEmail(user.getEmail())
                .imageUrl(requestDto.getImageUrl())
                .imageName(requestDto.getImageName())
                .build();

        inquiry = inquiryRepository.save(inquiry);

        // 첨부파일 처리
        if (requestDto.getAttachmentUrls() != null && !requestDto.getAttachmentUrls().isEmpty()) {
            saveAttachments(inquiry, requestDto.getAttachmentUrls());
        }

        log.info("문의 등록 완료: inquiryId={}, userId={}", inquiry.getId(), userId);
        return convertToResponseDto(inquiry);
    }

    public InquiryListResponseDto getInquiriesForUser(Pageable pageable, InquiryStatus status) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Page<Inquiry> inquiryPage;
        
        if (status != null) {
            inquiryPage = inquiryRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable);
        } else {
            inquiryPage = inquiryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        List<InquiryListItemDto> inquiries = inquiryPage.getContent().stream()
                .map(this::convertToListItemDto)
                .collect(Collectors.toList());

        InquiryListResponseDto.PaginationInfo pagination = InquiryListResponseDto.PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(inquiryPage.getTotalPages())
                .totalElements(inquiryPage.getTotalElements())
                .size(pageable.getPageSize())
                .hasNext(inquiryPage.hasNext())
                .hasPrevious(inquiryPage.hasPrevious())
                .build();

        return InquiryListResponseDto.builder()
                .inquiries(inquiries)
                .pagination(pagination)
                .build();
    }

    public InquiryResponseDto getInquiryDetailForUser(Long inquiryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Inquiry inquiry = inquiryRepository.findByIdAndUser(inquiryId, user)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));
        
        return convertToResponseDto(inquiry);
    }

    @Transactional
    public InquiryResponseDto updateInquiry(Long inquiryId, InquiryRequestDto requestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Inquiry inquiry = inquiryRepository.findByIdAndUser(inquiryId, user)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        if (inquiry.isReplied()) {
            throw new RuntimeException("답변이 완료된 문의는 수정할 수 없습니다.");
        }

        inquiry.setTitle(requestDto.getTitle());
        inquiry.setContent(requestDto.getContent());
        inquiry.setCategory(requestDto.getCategory());
        inquiry.setUserEmail(user.getEmail());
        inquiry.setImageUrl(requestDto.getImageUrl());
        inquiry.setImageName(requestDto.getImageName());

        // 기존 첨부파일 삭제 후 새로 등록
        attachmentRepository.deleteByInquiryId(inquiryId);
        if (requestDto.getAttachmentUrls() != null && !requestDto.getAttachmentUrls().isEmpty()) {
            saveAttachments(inquiry, requestDto.getAttachmentUrls());
        }

        log.info("문의 수정 완료: inquiryId={}, userId={}", inquiryId, userId);
        return convertToResponseDto(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Inquiry inquiry = inquiryRepository.findByIdAndUser(inquiryId, user)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        if (inquiry.isReplied()) {
            throw new RuntimeException("답변이 완료된 문의는 삭제할 수 없습니다.");
        }

        inquiryRepository.delete(inquiry);
        log.info("문의 삭제 완료: inquiryId={}, userId={}", inquiryId, userId);
    }

    // ===== 관리자용 메서드 =====

    public InquiryListResponseDto getInquiriesForAdmin(
            Pageable pageable, InquiryStatus status, InquiryCategory category, 
            InquiryPriority priority, Long adminId, String search) {
        
        Page<Inquiry> inquiryPage = inquiryRepository.findWithFilters(
                status, category, priority, adminId, search, pageable);

        List<InquiryListItemDto> inquiries = inquiryPage.getContent().stream()
                .map(this::convertToListItemDto)
                .collect(Collectors.toList());

        InquiryListResponseDto.PaginationInfo pagination = InquiryListResponseDto.PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(inquiryPage.getTotalPages())
                .totalElements(inquiryPage.getTotalElements())
                .size(pageable.getPageSize())
                .hasNext(inquiryPage.hasNext())
                .hasPrevious(inquiryPage.hasPrevious())
                .build();

        return InquiryListResponseDto.builder()
                .inquiries(inquiries)
                .pagination(pagination)
                .build();
    }

    public InquiryResponseDto getInquiryDetailForAdmin(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));
        
        return convertToResponseDto(inquiry);
    }

    @Transactional
    public void replyToInquiry(Long inquiryId, InquiryReplyRequestDto requestDto) {
        Long adminId = SecurityUtils.getCurrentUserId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));
        
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        inquiry.reply(admin, requestDto.getReply());

        if (requestDto.isCloseInquiry()) {
            inquiry.close();
        }

        log.info("문의 답변 완료: inquiryId={}, adminId={}", inquiryId, adminId);
    }

    @Transactional
    public void updateInquiryStatus(Long inquiryId, InquiryStatusUpdateDto requestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        inquiry.setStatus(requestDto.getStatus());
        
        if (requestDto.getStatus() == InquiryStatus.CLOSED) {
            inquiry.close();
        }

        log.info("문의 상태 변경: inquiryId={}, status={}", inquiryId, requestDto.getStatus());
    }

    @Transactional
    public void updateInquiryPriority(Long inquiryId, InquiryPriorityUpdateDto requestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        inquiry.changePriority(requestDto.getPriority());

        log.info("문의 우선순위 변경: inquiryId={}, priority={}", inquiryId, requestDto.getPriority());
    }

    @Transactional
    public void assignInquiry(Long inquiryId, InquiryAssignDto requestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        User admin = userRepository.findById(requestDto.getAdminId())
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        inquiry.assign(admin);

        log.info("문의 할당 완료: inquiryId={}, adminId={}", inquiryId, requestDto.getAdminId());
    }

    // ===== 통계 메서드 =====

    public InquiryStatsDto getInquiryStats() {
        long totalInquiries = inquiryRepository.count();
        long pendingInquiries = inquiryRepository.countByStatus(InquiryStatus.PENDING);
        long inProgressInquiries = inquiryRepository.countByStatus(InquiryStatus.IN_PROGRESS);
        long repliedInquiries = inquiryRepository.countByStatus(InquiryStatus.REPLIED);
        long closedInquiries = inquiryRepository.countByStatus(InquiryStatus.CLOSED);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long todayInquiries = inquiryRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long todayReplied = inquiryRepository.countByStatusAndCreatedAtBetween(
                InquiryStatus.REPLIED, todayStart, todayEnd);

        // 카테고리별 통계
        Map<String, Long> categoryStats = new HashMap<>();
        for (InquiryCategory category : InquiryCategory.values()) {
            categoryStats.put(category.getDescription(), inquiryRepository.countByCategory(category));
        }

        // 우선순위별 통계
        Map<String, Long> priorityStats = new HashMap<>();
        for (InquiryPriority priority : InquiryPriority.values()) {
            priorityStats.put(priority.getDescription(), inquiryRepository.countByPriority(priority));
        }

        // 최근 7일 통계
        Map<String, Long> recentDaysStats = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            long count = inquiryRepository.countByCreatedAtBetween(dayStart, dayEnd);
            recentDaysStats.put(date.toString(), count);
        }

        return InquiryStatsDto.builder()
                .totalInquiries(totalInquiries)
                .pendingInquiries(pendingInquiries)
                .inProgressInquiries(inProgressInquiries)
                .repliedInquiries(repliedInquiries)
                .closedInquiries(closedInquiries)
                .todayInquiries(todayInquiries)
                .todayReplied(todayReplied)
                .categoryStats(categoryStats)
                .priorityStats(priorityStats)
                .adminStats(new HashMap<>()) // TODO: 관리자별 통계 구현
                .averageResponseTime(0.0) // TODO: 평균 응답 시간 계산
                .recentDaysStats(recentDaysStats)
                .build();
    }

    public Map<String, Object> getUserInquiryStats() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        long totalInquiries = inquiryRepository.countByUser(user);
        long pendingInquiries = inquiryRepository.countByUserAndStatus(user, InquiryStatus.PENDING);
        long repliedInquiries = inquiryRepository.countByUserAndStatus(user, InquiryStatus.REPLIED);
        long closedInquiries = inquiryRepository.countByUserAndStatus(user, InquiryStatus.CLOSED);

        return Map.of(
                "totalInquiries", totalInquiries,
                "pendingInquiries", pendingInquiries,
                "repliedInquiries", repliedInquiries,
                "closedInquiries", closedInquiries
        );
    }

    // ===== 공통 메서드 =====

    public List<Map<String, Object>> getInquiryCategories() {
        return Arrays.stream(InquiryCategory.values())
                .map(category -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", category.name());
                    map.put("name", category.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getInquiryPriorities() {
        return Arrays.stream(InquiryPriority.values())
                .map(priority -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", priority.name());
                    map.put("name", priority.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getInquiryStatuses() {
        return Arrays.stream(InquiryStatus.values())
                .map(status -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", status.name());
                    map.put("name", status.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ===== private 헬퍼 메서드 =====

    private void saveAttachments(Inquiry inquiry, List<String> attachmentUrls) {
        for (int i = 0; i < attachmentUrls.size(); i++) {
            String url = attachmentUrls.get(i);
            InquiryAttachment attachment = InquiryAttachment.builder()
                    .inquiry(inquiry)
                    .originalFileName("attachment_" + (i + 1))
                    .storedFileName("stored_" + (i + 1))
                    .fileUrl(url)
                    .fileSize(0L)
                    .contentType("application/octet-stream")
                    .orderNumber(i)
                    .build();
            attachmentRepository.save(attachment);
        }
    }

    private InquiryResponseDto convertToResponseDto(Inquiry inquiry) {
        List<InquiryAttachmentDto> attachmentDtos = inquiry.getAttachments() != null ?
                inquiry.getAttachments().stream()
                        .map(this::convertToAttachmentDto)
                        .collect(Collectors.toList()) : new ArrayList<>();

        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .category(inquiry.getCategory())
                .categoryName(inquiry.getCategory().getDescription())
                .status(inquiry.getStatus())
                .statusName(inquiry.getStatus().getDescription())
                .priority(inquiry.getPriority())
                .priorityName(inquiry.getPriority().getDescription())
                .userEmail(inquiry.getUserEmail())
                .userId(inquiry.getUser().getId())
                .userName(inquiry.getUser().getName())
                .assignedAdminId(inquiry.getAssignedAdmin() != null ? inquiry.getAssignedAdmin().getId() : null)
                .assignedAdminName(inquiry.getAssignedAdmin() != null ? inquiry.getAssignedAdmin().getName() : null)
                .adminReply(inquiry.getAdminReply())
                .repliedAt(inquiry.getRepliedAt())
                .createdDate(inquiry.getCreatedAt())
                .modifiedDate(inquiry.getUpdatedAt())
                .closedAt(inquiry.getClosedAt())
                .attachments(attachmentDtos)
                .imageUrl(inquiry.getImageUrl())
                .imageName(inquiry.getImageName())
                .isReplied(inquiry.isReplied())
                .isClosed(inquiry.isClosed())
                .build();
    }

    private InquiryListItemDto convertToListItemDto(Inquiry inquiry) {
        return InquiryListItemDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .category(inquiry.getCategory())
                .categoryName(inquiry.getCategory().getDescription())
                .status(inquiry.getStatus())
                .statusName(inquiry.getStatus().getDescription())
                .priority(inquiry.getPriority())
                .priorityName(inquiry.getPriority().getDescription())
                .userId(inquiry.getUser().getId())
                .userName(inquiry.getUser().getName())
                .userEmail(inquiry.getUserEmail())
                .assignedAdminId(inquiry.getAssignedAdmin() != null ? inquiry.getAssignedAdmin().getId() : null)
                .assignedAdminName(inquiry.getAssignedAdmin() != null ? inquiry.getAssignedAdmin().getName() : null)
                .createdDate(inquiry.getCreatedAt())
                .repliedAt(inquiry.getRepliedAt())
                .isReplied(inquiry.isReplied())
                .isClosed(inquiry.isClosed())
                .hasAttachments(inquiry.getAttachments() != null && !inquiry.getAttachments().isEmpty())
                .build();
    }

    private InquiryAttachmentDto convertToAttachmentDto(InquiryAttachment attachment) {
        return InquiryAttachmentDto.builder()
                .id(attachment.getId())
                .originalFileName(attachment.getOriginalFileName())
                .fileUrl(attachment.getFileUrl())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .orderNumber(attachment.getOrderNumber())
                .build();
    }
}