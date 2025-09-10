package com.dataury.soloJ.domain.mypage.service;

import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import com.dataury.soloJ.domain.chat.service.MessageReadQueryService;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.service.PostService;
import com.dataury.soloJ.domain.community.service.ScrapService;
import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.plan.dto.PlanResponseDto;
import com.dataury.soloJ.domain.plan.service.PlanService;
import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.service.ReviewService;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {
    private final ChatRoomQueryService chatRoomQueryService;
    private final MessageReadQueryService messageReadQueryService;
    private final ScrapService scrapService;
    private final PostService postService;
    private final ReviewService reviewService;
    private final PlanService planService;

    public Page<HomeResponse.OpenChatRoomDto> getMyChatRooms(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        var chatRooms = chatRoomQueryService.getMyChatRooms(userId, pageable);
        return chatRooms.map(room -> HomeResponse.OpenChatRoomDto.builder()
                .roomId(room.getChatRoomId())
                .title(room.getTitle())
                .description(room.getDescription())
                .isCompleted(room.getIsCompleted())
                .spotName(room.getSpotName())
                .spotImage(room.getTouristSpotImage())
                .currentParticipants(room.getCurrentMembers() != null ? room.getCurrentMembers().intValue() : 0)
                .maxParticipants(room.getMaxMembers() != null ? room.getMaxMembers().intValue() : 10)
                .scheduledDate(room.getJoinDate())
                .genderRestriction(room.getGenderRestriction())
                .hasUnreadMessages(room.getHasUnreadMessages()) // 안읽은 메시지 여부 추가
                .build());
    }

    public Page<PostResponseDto.PostListItemDto> getMyScrapList(Pageable pageable){
        Long userId = SecurityUtils.getCurrentUserId();
        return scrapService.getMyScrapList(userId, pageable);
    }

    public CursorPageResponse<PostResponseDto.PostListItemDto> getMyScrapListByCursor(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return scrapService.getMyScrapListByCursor(userId, cursor, size);
    }

    public Page<PostResponseDto.PostListItemDto> getMyPosts(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return postService.getMyPosts(userId, pageable);
    }

    public Page<PostResponseDto.PostListItemDto> getMyCommentedPosts(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return postService.getPostsWithMyComments(userId, pageable);
    }

    // 커서 기반 페이지네이션 메서드들
    public CursorPageResponse<HomeResponse.OpenChatRoomDto> getMyChatRoomsByCursor(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        var response = chatRoomQueryService.getMyChatRoomsByCursor(userId, cursor, size);
        var convertedContent = response.getContent().stream()
                .map(room -> HomeResponse.OpenChatRoomDto.builder()
                        .roomId(room.getChatRoomId())
                        .title(room.getTitle())
                        .description(room.getDescription())
                        .isCompleted(room.getIsCompleted())
                        .spotName(room.getSpotName())
                        .spotImage(room.getTouristSpotImage())
                        .currentParticipants(room.getCurrentMembers() != null ? room.getCurrentMembers().intValue() : 0)
                        .maxParticipants(room.getMaxMembers() != null ? room.getMaxMembers().intValue() : 10)
                        .scheduledDate(room.getJoinDate())
                        .genderRestriction(room.getGenderRestriction())
                        .hasUnreadMessages(room.getHasUnreadMessages()) // 안읽은 메시지 여부 추가
                        .build())
                .toList();
        return CursorPageResponse.<HomeResponse.OpenChatRoomDto>builder()
                .content(convertedContent)
                .nextCursor(response.getNextCursor())
                .hasNext(response.isHasNext())
                .size(convertedContent.size())
                .build();
    }

    public CursorPageResponse<PostResponseDto.PostListItemDto> getMyPostsByCursor(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return postService.getMyPostsByCursor(userId, cursor, size);
    }

    public CursorPageResponse<PostResponseDto.PostListItemDto> getMyCommentedPostsByCursor(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return postService.getPostsWithMyCommentsByCursor(userId, cursor, size);
    }

    public Page<ReviewResponseDto.ReviewListDto> getMyReviews(Pageable pageable) {
        return reviewService.getMyReviews(pageable);
    }

    public CursorPageResponse<ReviewResponseDto.ReviewListDto> getMyReviewsByCursor(String cursor, int size) {
        return reviewService.getMyReviewsByCursor(cursor, size);
    }
    
    public Page<PlanResponseDto.PlanListItemDto> getMyPlans(Pageable pageable) {
        return planService.getMyPlans(pageable);
    }
    
    public CursorPageResponse<PlanResponseDto.PlanListItemDto> getMyPlansByCursor(String cursor, int size) {
        return planService.getMyPlansByCursor(cursor, size);
    }
}
