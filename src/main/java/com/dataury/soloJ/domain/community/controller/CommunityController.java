package com.dataury.soloJ.domain.community.controller;

import com.dataury.soloJ.domain.community.dto.CommentRequestDto;
import com.dataury.soloJ.domain.community.dto.CommentResponseDto;
import com.dataury.soloJ.domain.community.dto.PostRequestDto;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.entity.status.PostCategory;
import com.dataury.soloJ.domain.community.service.CommentService;
import com.dataury.soloJ.domain.community.service.PostService;
import com.dataury.soloJ.domain.community.service.ScrapService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "Community API", description = "커뮤니티 관련 API")
public class CommunityController {

    private final PostService postService;
    private final CommentService commentService;
    private final ScrapService scrapService;

    @PostMapping("/posts")
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    public ApiResponse<PostResponseDto.PostCreateResponseDto> createPost(
            @Valid @RequestBody PostRequestDto.CreatePostDto request) {
        return ApiResponse.onSuccess(postService.createPost(request));
    }

    @PatchMapping("/posts/{postId}")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    public ApiResponse<PostResponseDto.PostCreateResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto.UpdatePostDto request) {
        return ApiResponse.onSuccess(postService.updatePost(postId, request));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    public ApiResponse<String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.onSuccess("게시글이 삭제되었습니다.");
    }

    @GetMapping("/posts")
    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 조회합니다. 커서가 제공되면 커서 기반 페이지네이션을 사용하고, 없으면 offset 기반을 사용합니다.")
    public ApiResponse<?> getPostList(
            @Parameter(description = "카테고리") @RequestParam(required = false) PostCategory category,
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 번호 (0부터 시작, offset 기반용)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (offset 기반용)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향 (offset 기반용)") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            // 커서 기반 페이지네이션
            return ApiResponse.onSuccess(postService.getPostListByCursor(category, cursor, size));
        } else {
            // 기존 offset 기반 페이지네이션
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
            return ApiResponse.onSuccess(postService.getPostList(category, pageable));
        }
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    public ApiResponse<PostResponseDto.PostDetailDto> getPostDetail(@PathVariable Long postId) {
        return ApiResponse.onSuccess(postService.getPostDetail(postId));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "게시글 검색", description = "키워드로 게시글을 검색합니다. 커서가 제공되면 커서 기반 페이지네이션을 사용하고, 없으면 offset 기반을 사용합니다.")
    public ApiResponse<?> searchPosts(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 번호 (offset 기반용)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            // 커서 기반 페이지네이션
            return ApiResponse.onSuccess(postService.searchPostsByCursor(keyword, cursor, size));
        } else {
            // 기존 offset 기반 페이지네이션
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return ApiResponse.onSuccess(postService.searchPosts(keyword, pageable));
        }
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public ApiResponse<CommentResponseDto.CommentCreateResponseDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto.CreateCommentDto request) {
        return ApiResponse.onSuccess(commentService.createComment(postId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public ApiResponse<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.onSuccess("댓글이 삭제되었습니다.");
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 댓글 목록을 커서 기반 페이지네이션으로 조회합니다.")
    public ApiResponse<CursorPageResponse<CommentResponseDto.CommentDto>> getCommentsByPostId(
            @PathVariable Long postId,
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.onSuccess(commentService.getCommentsByPostId(postId, cursor, size));
    }

    @PutMapping("/posts/{postId}/scrap")
    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩하거나 스크랩을 취소합니다.")
    public ApiResponse<String> toggleScrap(@PathVariable Long postId) {
        return ApiResponse.onSuccess(scrapService.toggleScrap(postId));
    }

}