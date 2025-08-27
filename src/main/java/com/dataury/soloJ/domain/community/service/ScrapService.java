package com.dataury.soloJ.domain.community.service;

import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.entity.Scrap;
import com.dataury.soloJ.domain.community.repository.CommentRepository;
import com.dataury.soloJ.domain.community.repository.PostRepository;
import com.dataury.soloJ.domain.community.repository.ScrapRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public String toggleScrap(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        Optional<Scrap> existingScrap = scrapRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingScrap.isPresent()) {
            scrapRepository.delete(existingScrap.get());
            return "스크랩이 취소되었습니다.";
        } else {
            Scrap scrap = Scrap.builder()
                    .user(user)
                    .post(post)
                    .build();
            scrapRepository.save(scrap);
            return "게시글이 스크랩되었습니다.";
        }
    }

    public Page<PostResponseDto.PostListItemDto> getMyScrapList(Long userId, Pageable pageable) {
        Page<Scrap> scraps = scrapRepository.findByUserIdWithPost(userId, pageable);
        
        return scraps.map(scrap -> convertToPostListItemDto(scrap));
    }

    // 커서 기반 스크랩 목록 조회
    public CursorPageResponse<PostResponseDto.PostListItemDto> getMyScrapListByCursor(Long userId, String cursor, int size) {
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1);
        
        List<Scrap> scraps = scrapRepository.findByUserIdWithPostByCursor(userId, cursorDateTime, pageable);
        
        boolean hasNext = scraps.size() > size;
        if (hasNext) {
            scraps = scraps.subList(0, size);
        }
        
        List<PostResponseDto.PostListItemDto> items = scraps.stream()
                .map(this::convertToPostListItemDto)
                .collect(Collectors.toList());
        
        String nextCursor = hasNext && !scraps.isEmpty() 
                ? encodeCursor(scraps.get(scraps.size() - 1).getCreatedAt()) 
                : null;
        
        return CursorPageResponse.<PostResponseDto.PostListItemDto>builder()
                .content(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(items.size())
                .build();
    }

    // Scrap을 PostListItemDto로 변환하는 공통 메서드
    private PostResponseDto.PostListItemDto convertToPostListItemDto(Scrap scrap) {
        Post post = scrap.getPost();
        UserProfile authorProfile = userProfileRepository.findByUser(post.getUser())
                .orElse(null);
        
        return PostResponseDto.PostListItemDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent().length() > 100 
                        ? post.getContent().substring(0, 100) + "..." 
                        : post.getContent())
                .postCategory(post.getPostCategory())
                .authorNickname(authorProfile != null ? authorProfile.getNickName() : "익명")
                .authorId(post.getUser().getId())
                .authorProfileImage(authorProfile != null ? authorProfile.getImageUrl() : null)
                .commentCount(commentRepository.countByPostId(post.getId()))
                .scrapCount(scrapRepository.countByPostId(post.getId()))
                .createdAt(post.getCreatedAt())
                .thumbnailUrl(post.getThumbnailUrl())
                .build();
    }

    private String encodeCursor(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        String formatted = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Base64.getEncoder().encodeToString(formatted.getBytes());
    }

    private LocalDateTime decodeCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return LocalDateTime.parse(decoded, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}