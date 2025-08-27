package com.dataury.soloJ.domain.inquiry.controller;

import com.dataury.soloJ.domain.inquiry.dto.*;
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
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Tag(name = "문의 관리", description = "1:1 문의 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @Operation(summary = "문의 등록", description = "새로운 1:1 문의를 등록합니다.")
    public ApiResponse<InquiryResponseDto> createInquiry(
            @Valid @RequestBody InquiryRequestDto requestDto) {
        InquiryResponseDto response = inquiryService.createInquiry(requestDto);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/my")
    @Operation(summary = "내 문의 목록 조회", description = "로그인한 사용자의 문의 목록을 조회합니다.")
    public ApiResponse<InquiryListResponseDto> getMyInquiries(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "상태 필터")
            @RequestParam(required = false) InquiryStatus status) {

        Pageable pageable = PageRequest.of(page - 1, size);
        InquiryListResponseDto response = inquiryService.getInquiriesForUser(pageable, status);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "내 문의 상세 조회", description = "내가 등록한 문의의 상세 정보를 조회합니다.")
    public ApiResponse<InquiryResponseDto> getMyInquiryDetail(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id) {
        InquiryResponseDto response = inquiryService.getInquiryDetailForUser(id);
        return ApiResponse.onSuccess(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "문의 수정", description = "답변이 오기 전인 문의를 수정합니다.")
    public ApiResponse<InquiryResponseDto> updateInquiry(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InquiryRequestDto requestDto) {
        InquiryResponseDto response = inquiryService.updateInquiry(id, requestDto);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "문의 삭제", description = "답변이 오기 전인 문의를 삭제합니다.")
    public ApiResponse<String> deleteInquiry(
            @Parameter(description = "문의 ID", required = true)
            @PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ApiResponse.onSuccess("문의가 삭제되었습니다.");
    }

    @GetMapping("/categories")
    @Operation(summary = "문의 카테고리 목록", description = "문의 등록시 사용 가능한 카테고리 목록을 조회합니다.")
    public ApiResponse<Object> getInquiryCategories() {
        return ApiResponse.onSuccess(inquiryService.getInquiryCategories());
    }

    @GetMapping("/my/stats")
    @Operation(summary = "내 문의 통계", description = "로그인한 사용자의 문의 통계를 조회합니다.")
    public ApiResponse<Object> getMyInquiryStats() {
        return ApiResponse.onSuccess(inquiryService.getUserInquiryStats());
    }
}