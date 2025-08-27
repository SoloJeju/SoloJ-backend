package com.dataury.soloJ.domain.inquiry.controller;

import com.dataury.soloJ.domain.inquiry.dto.*;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import com.dataury.soloJ.domain.inquiry.service.InquiryService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
@Tag(name = "관리자 - 문의 관리", description = "1:1 문의 관리 API")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    @Operation(summary = "문의 목록 조회", description = "필터링과 검색이 가능한 문의 목록을 조회합니다.")
    public ApiResponse<InquiryListResponseDto> getInquiries(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "상태 필터")
            @RequestParam(required = false) InquiryStatus status,
            @Parameter(description = "카테고리 필터")
            @RequestParam(required = false) InquiryCategory category,
            @Parameter(description = "우선순위 필터")
            @RequestParam(required = false) InquiryPriority priority,
            @Parameter(description = "담당 관리자 ID")
            @RequestParam(required = false) Long adminId,
            @Parameter(description = "검색어 (제목, 내용)")
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page - 1, size);
        InquiryListResponseDto response = inquiryService.getInquiriesForAdmin(
            pageable, status, category, priority, adminId, search);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "문의 상세 조회", description = "특정 문의의 상세 정보를 조회합니다.")
    public ApiResponse<InquiryResponseDto> getInquiryDetail(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id) {
        InquiryResponseDto response = inquiryService.getInquiryDetailForAdmin(id);
        return ApiResponse.onSuccess(response);
    }

    @PutMapping("/{id}/reply")
    @Operation(summary = "문의 답변", description = "문의에 답변을 작성합니다.")
    public ApiResponse<String> replyToInquiry(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InquiryReplyRequestDto requestDto) {
        inquiryService.replyToInquiry(id, requestDto);
        return ApiResponse.onSuccess("답변이 등록되었습니다.");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "문의 상태 변경", description = "문의의 상태를 변경합니다.")
    public ApiResponse<String> updateInquiryStatus(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InquiryStatusUpdateDto requestDto) {
        inquiryService.updateInquiryStatus(id, requestDto);
        return ApiResponse.onSuccess("상태가 변경되었습니다.");
    }

    @PutMapping("/{id}/priority")
    @Operation(summary = "문의 우선순위 변경", description = "문의의 우선순위를 변경합니다.")
    public ApiResponse<String> updateInquiryPriority(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InquiryPriorityUpdateDto requestDto) {
        inquiryService.updateInquiryPriority(id, requestDto);
        return ApiResponse.onSuccess("우선순위가 변경되었습니다.");
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "문의 할당", description = "문의를 특정 관리자에게 할당합니다.")
    public ApiResponse<String> assignInquiry(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InquiryAssignDto requestDto) {
        inquiryService.assignInquiry(id, requestDto);
        return ApiResponse.onSuccess("문의가 할당되었습니다.");
    }

    @GetMapping("/stats")
    @Operation(summary = "문의 통계", description = "문의 관련 전체 통계를 조회합니다.")
    public ApiResponse<InquiryStatsDto> getInquiryStats() {
        InquiryStatsDto stats = inquiryService.getInquiryStats();
        return ApiResponse.onSuccess(stats);
    }

    @GetMapping("/categories")
    @Operation(summary = "문의 카테고리 목록", description = "사용 가능한 문의 카테고리 목록을 조회합니다.")
    public ApiResponse<Object> getInquiryCategories() {
        return ApiResponse.onSuccess(inquiryService.getInquiryCategories());
    }

    @GetMapping("/priorities")
    @Operation(summary = "문의 우선순위 목록", description = "사용 가능한 우선순위 목록을 조회합니다.")
    public ApiResponse<Object> getInquiryPriorities() {
        return ApiResponse.onSuccess(inquiryService.getInquiryPriorities());
    }

    @GetMapping("/statuses")
    @Operation(summary = "문의 상태 목록", description = "사용 가능한 상태 목록을 조회합니다.")
    public ApiResponse<Object> getInquiryStatuses() {
        return ApiResponse.onSuccess(inquiryService.getInquiryStatuses());
    }
}