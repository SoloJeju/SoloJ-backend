package com.dataury.soloJ.domain.community.controller;

import com.dataury.soloJ.domain.community.dto.CommentResponseDto;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.service.CommentService;
import com.dataury.soloJ.domain.community.service.PostService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
@Tag(name = "관리자 - 커뮤니티 관리", description = "관리자용 게시글/댓글 조회 및 관리 API")
public class AdminCommunityController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/posts/{postId}")
    @Operation(summary = "관리자 - 게시글 상세 조회", 
               description = "관리자가 제한 없이 모든 게시글(숨김/삭제 포함)과 댓글의 현재 상태를 조회합니다.")
    public ApiResponse<PostResponseDto.AdminPostDetailDto> getPostForAdmin(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId) {
        
        PostResponseDto.AdminPostDetailDto postDetail = postService.getPostDetailForAdmin(postId);
        return ApiResponse.onSuccess(postDetail);
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "관리자 - 댓글 상세 조회", 
               description = "관리자가 댓글 상세 정보와 최소한의 게시글 맥락(ID, 제목, 작성자)을 조회합니다.")
    public ApiResponse<CommentResponseDto.AdminCommentDetailDto> getCommentForAdmin(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId) {
        
        CommentResponseDto.AdminCommentDetailDto commentDetail = commentService.getCommentDetailForAdmin(commentId);
        return ApiResponse.onSuccess(commentDetail);
    }
}