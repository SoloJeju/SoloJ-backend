package com.dataury.soloJ.domain.community.service;

import com.dataury.soloJ.domain.community.dto.CommentResponseDto;
import com.dataury.soloJ.domain.community.dto.PostRequestDto;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.entity.PostImage;
import com.dataury.soloJ.domain.community.entity.status.PostCategory;
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
import com.dataury.soloJ.global.security.UserPenaltyChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPenaltyChecker userPenaltyChecker;

    @Transactional
    public PostResponseDto.PostCreateResponseDto createPost(PostRequestDto.CreatePostDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 사용자 제재 상태 확인
        userPenaltyChecker.checkCommunityWritePermission(userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 썸네일 설정 (첫 번째 이미지)
        String thumbnailUrl = null;
        String thumbnailName = null;
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            thumbnailUrl = request.getImageUrls().get(0);
            thumbnailName = request.getImageNames() != null && !request.getImageNames().isEmpty() 
                    ? request.getImageNames().get(0) : null;
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postCategory(request.getPostCategory())
                .thumbnailUrl(thumbnailUrl)
                .thumbnailName(thumbnailName)
                .user(user)
                .build();

        // 이미지 리스트 생성
        List<PostImage> images = new ArrayList<>();
        if (request.getImageUrls() != null && request.getImageNames() != null) {
            int size = Math.min(request.getImageUrls().size(), request.getImageNames().size());
            for (int i = 0; i < size; i++) {
                PostImage image = PostImage.builder()
                        .imageUrl(request.getImageUrls().get(i))
                        .imageName(request.getImageNames().get(i))
                        .post(post)
                        .build();
                images.add(image);
            }
        }
        post.updateImages(images);

        Post savedPost = postRepository.save(post);

        return PostResponseDto.PostCreateResponseDto.builder()
                .postId(savedPost.getId())
                .message("게시글이 성공적으로 작성되었습니다.")
                .build();
    }

    @Transactional
    public PostResponseDto.PostCreateResponseDto updatePost(Long postId, PostRequestDto.UpdatePostDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 사용자 제재 상태 확인
        userPenaltyChecker.checkCommunityWritePermission(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        if (!post.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        post.updatePost(request.getTitle(), request.getContent(), request.getPostCategory());

        // 삭제 요청 처리
        if (request.getDeleteImageNames() != null && !request.getDeleteImageNames().isEmpty()) {
            List<PostImage> remainImages = post.getImages().stream()
                    .filter(img -> !request.getDeleteImageNames().contains(img.getImageName()))
                    .collect(Collectors.toList());
            post.updateImages(remainImages);
        }

        // 교체(전체 새로 보냄) 로직
        if (request.getNewImageUrls() != null) {
            List<PostImage> newImages = new ArrayList<>();
            if (request.getNewImageNames() != null) {
                int size = Math.min(request.getNewImageUrls().size(), request.getNewImageNames().size());
                for (int i = 0; i < size; i++) {
                    PostImage image = PostImage.builder()
                            .imageUrl(request.getNewImageUrls().get(i))
                            .imageName(request.getNewImageNames().get(i))
                            .post(post)
                            .build();
                    newImages.add(image);
                }
            }
            post.updateImages(newImages);
        }

        // 썸네일 갱신
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            PostImage first = post.getImages().get(0);
            post.updateThumbnail(first.getImageUrl(), first.getImageName());
        } else {
            post.updateThumbnail(null, null);
        }

        return PostResponseDto.PostCreateResponseDto.builder()
                .postId(post.getId())
                .message("게시글이 성공적으로 수정되었습니다.")
                .build();
    }

    @Transactional
    public void deletePost(Long postId) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        
        boolean isAdmin = SecurityUtils.isAdmin();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        // 작성자 본인이거나 관리자인 경우에만 삭제 가능
        if (!post.getUser().getId().equals(userId) && !isAdmin) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        commentRepository.deleteByPostId(postId);
        scrapRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    public Page<PostResponseDto.PostListItemDto> getPostList(PostCategory category, Pageable pageable) {
        Page<Post> posts = (category != null) 
                ? postRepository.findByPostCategoryAndIsVisibleTrueAndIsDeletedFalse(category, pageable)
                : postRepository.findByIsVisibleTrueAndIsDeletedFalse(pageable);

        return posts.map(this::convertToListItemDto);
    }

    public PostResponseDto.PostDetailDto getPostDetail(Long postId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        List<CommentResponseDto.CommentDto> comments = commentRepository.findByPostIdWithUser(postId)
                .stream()
                .map(comment -> {
                    // 삭제된 댓글인 경우
                    if (comment.isDeleted()) {
                        return CommentResponseDto.CommentDto.builder()
                                .commentId(comment.getId())
                                .content("삭제된 댓글입니다.")
                                .isDeleted(true)
                                .createdAt(comment.getCreatedAt())
                                .build();
                    }
                    
                    // 삭제되지 않은 댓글인 경우
                    UserProfile profile = userProfileRepository.findByUser(comment.getUser())
                            .orElse(null);
                    
                    return CommentResponseDto.CommentDto.builder()
                            .commentId(comment.getId())
                            .content(comment.getContent())
                            .authorNickname(profile != null ? profile.getNickName() : "익명")
                            .authorId(comment.getUser().getId())
                            .authorProfileImage(profile != null ? profile.getImageUrl() : null)
                            .isMine(currentUserId != null && comment.getUser().getId().equals(currentUserId))
                            .isDeleted(false)
                            .createdAt(comment.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        UserProfile authorProfile = userProfileRepository.findByUser(post.getUser())
                .orElse(null);

        return PostResponseDto.PostDetailDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .postCategory(post.getPostCategory())
                .authorNickname(authorProfile != null ? authorProfile.getNickName() : "익명")
                .authorId(post.getUser().getId())
                .authorProfileImage(authorProfile != null ? authorProfile.getImageUrl() : null)
                .commentCount(commentRepository.countByPostId(postId))
                .scrapCount(scrapRepository.countByPostId(postId))
                .isScraped(currentUserId != null && scrapRepository.existsByUserIdAndPostId(currentUserId, postId))
                .isMine(currentUserId != null && post.getUser().getId().equals(currentUserId))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .thumbnailUrl(post.getThumbnailUrl())
                .thumbnailName(post.getThumbnailName())
                .images(post.getImages() != null ? post.getImages().stream()
                        .map(img -> PostResponseDto.ImageDto.builder()
                                .imageUrl(img.getImageUrl())
                                .imageName(img.getImageName())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .comments(comments)
                .build();
    }

    public Page<PostResponseDto.PostListItemDto> searchPosts(String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchByKeyword(keyword, pageable);
        
        return posts.map(this::convertToListItemDto);
    }

    public Page<PostResponseDto.PostListItemDto> getMyPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdAndIsVisibleTrueAndIsDeletedFalse(userId, pageable);
        return posts.map(this::convertToListItemDto);
    }

    public Page<PostResponseDto.PostListItemDto> getPostsWithMyComments(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findCommentedPostsOrderByLatestMyComment(userId, pageable);
        return posts.map(this::convertToListItemDto);
    }

    // ===== 커서 기반 페이지네이션 메서드들 =====
    
    public CursorPageResponse<PostResponseDto.PostListItemDto> getPostListByCursor(PostCategory category, String cursor, int size) {
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1);
        
        List<Post> posts = (category != null) 
                ? postRepository.findByCategoryAndCursor(category, cursorDateTime, pageable)
                : postRepository.findAllByCursor(cursorDateTime, pageable);
        
        return buildCursorPageResponse(posts, size);
    }
    
    public CursorPageResponse<PostResponseDto.PostListItemDto> searchPostsByCursor(String keyword, String cursor, int size) {
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1);
        
        List<Post> posts = postRepository.searchByKeywordAndCursor(keyword, cursorDateTime, pageable);
        
        return buildCursorPageResponse(posts, size);
    }
    
    public CursorPageResponse<PostResponseDto.PostListItemDto> getMyPostsByCursor(Long userId, String cursor, int size) {
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1);
        
        List<Post> posts = postRepository.findByUserIdAndCursor(userId, cursorDateTime, pageable);
        
        return buildCursorPageResponse(posts, size);
    }
    
    public CursorPageResponse<PostResponseDto.PostListItemDto> getPostsWithMyCommentsByCursor(Long userId, String cursor, int size) {
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1);
        
        List<Post> posts = postRepository.findCommentedPostsByUserIdAndCursor(userId, cursorDateTime, pageable);
        
        return buildCursorPageResponse(posts, size);
    }

    private CursorPageResponse<PostResponseDto.PostListItemDto> buildCursorPageResponse(List<Post> posts, int size) {
        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts = posts.subList(0, size);
        }
        
        List<PostResponseDto.PostListItemDto> items = posts.stream()
                .map(this::convertToListItemDto)
                .collect(Collectors.toList());
        
        String nextCursor = hasNext && !posts.isEmpty() 
                ? encodeCursor(posts.get(posts.size() - 1).getCreatedAt()) 
                : null;
        
        return CursorPageResponse.<PostResponseDto.PostListItemDto>builder()
                .content(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(items.size())
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

    // ===== 관리자용 메서드 =====
    
    public PostResponseDto.AdminPostDetailDto getPostDetailForAdmin(Long postId) {
        Post post = postRepository.findByIdWithUserForAdmin(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        // 모든 댓글 조회 (숨김/삭제 포함)
        List<PostResponseDto.AdminCommentDto> comments = commentRepository.findByPostIdWithUserForAdmin(postId)
                .stream()
                .map(comment -> {
                    UserProfile profile = userProfileRepository.findByUser(comment.getUser())
                            .orElse(null);

                    String commentStatus = "visible";
                    if (comment.isDeleted()) {
                        commentStatus = "deleted";
                    } else if (!comment.isVisible()) {
                        commentStatus = "hidden";
                    }
                    
                    return PostResponseDto.AdminCommentDto.builder()
                            .commentId(comment.getId())
                            .content(comment.getContent())
                            .originalContent(comment.getOriginalContent())
                            .authorNickname(profile != null ? profile.getNickName() : "익명")
                            .authorId(comment.getUser().getId())
                            .authorProfileImage(profile != null ? profile.getImageUrl() : null)
                            .isVisible(comment.isVisible())
                            .isDeleted(comment.isDeleted())
                            .status(commentStatus)
                            .createdAt(comment.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        UserProfile authorProfile = userProfileRepository.findByUser(post.getUser())
                .orElse(null);

        // 게시글 상태 결정
        String postStatus = "visible";
        if (post.isDeleted()) {
            postStatus = "deleted";
        } else if (!post.isVisible()) {
            postStatus = "hidden";
        }

        return PostResponseDto.AdminPostDetailDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .postCategory(post.getPostCategory())
                .authorNickname(authorProfile != null ? authorProfile.getNickName() : "익명")
                .authorId(post.getUser().getId())
                .authorProfileImage(authorProfile != null ? authorProfile.getImageUrl() : null)
                .commentCount(commentRepository.countByPostIdForAdmin(postId))
                .scrapCount(scrapRepository.countByPostId(postId))
                .isVisible(post.isVisible())
                .isDeleted(post.isDeleted())
                .status(postStatus)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .thumbnailUrl(post.getThumbnailUrl())
                .thumbnailName(post.getThumbnailName())
                .images(post.getImages() != null ? post.getImages().stream()
                        .map(img -> PostResponseDto.ImageDto.builder()
                                .imageUrl(img.getImageUrl())
                                .imageName(img.getImageName())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .comments(comments)
                .build();
    }

    private PostResponseDto.PostListItemDto convertToListItemDto(Post post) {
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
}