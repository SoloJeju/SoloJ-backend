package com.dataury.soloJ.domain.community.service;

import com.dataury.soloJ.domain.community.dto.CommentResponseDto;
import com.dataury.soloJ.domain.community.dto.PostRequestDto;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.entity.Post;
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
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public PostResponseDto.PostCreateResponseDto createPost(PostRequestDto.CreatePostDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postCategory(request.getPostCategory())
                .imageUrl(request.getImageUrl())
                .imageName(request.getImageName())
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        return PostResponseDto.PostCreateResponseDto.builder()
                .postId(savedPost.getId())
                .message("게시글이 성공적으로 작성되었습니다.")
                .build();
    }

    @Transactional
    public PostResponseDto.PostCreateResponseDto updatePost(Long postId, PostRequestDto.UpdatePostDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        if (!post.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getPostCategory() != null) {
            post.setPostCategory(request.getPostCategory());
        }
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
            post.setImageName(request.getImageName());
        }

        return PostResponseDto.PostCreateResponseDto.builder()
                .postId(post.getId())
                .message("게시글이 성공적으로 수정되었습니다.")
                .build();
    }

    @Transactional
    public void deletePost(Long postId) {
        System.out.println("===== deletePost 시작 =====");
        System.out.println("삭제 요청 게시글 ID: " + postId);
        
        Long userId = SecurityUtils.getCurrentUserId();
        System.out.println("현재 사용자 ID: " + userId);
        
        boolean isAdmin = SecurityUtils.isAdmin();
        System.out.println("관리자 여부: " + isAdmin);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        System.out.println("게시글 작성자 ID: " + post.getUser().getId());
        System.out.println("본인 게시글 여부: " + post.getUser().getId().equals(userId));
        
        // 작성자 본인이거나 관리자인 경우에만 삭제 가능
        if (!post.getUser().getId().equals(userId) && !isAdmin) {
            System.out.println("삭제 권한 없음 - FORBIDDEN 발생");
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        commentRepository.deleteByPostId(postId);
        scrapRepository.deleteByPostId(postId);
        postRepository.delete(post);
        System.out.println("게시글 삭제 완료");
        System.out.println("===== deletePost 종료 =====");
    }

    public Page<PostResponseDto.PostListItemDto> getPostList(PostCategory category, Pageable pageable) {
        Page<Post> posts = (category != null) 
                ? postRepository.findByPostCategory(category, pageable)
                : postRepository.findAll(pageable);

        return posts.map(this::convertToListItemDto);
    }

    public PostResponseDto.PostDetailDto getPostDetail(Long postId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        List<CommentResponseDto.CommentDto> comments = commentRepository.findByPostIdWithUser(postId)
                .stream()
                .map(comment -> {
                    UserProfile profile = userProfileRepository.findByUser(comment.getUser())
                            .orElse(null);
                    
                    return CommentResponseDto.CommentDto.builder()
                            .commentId(comment.getId())
                            .content(comment.getContent())
                            .authorNickname(profile != null ? profile.getNickName() : "익명")
                            .authorId(comment.getUser().getId())
                            .authorProfileImage(profile != null ? profile.getImage() : null)
                            .isMine(currentUserId != null && comment.getUser().getId().equals(currentUserId))
                            .isDeleted(comment.isDeleted())
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
                .authorProfileImage(authorProfile != null ? authorProfile.getImage() : null)
                .commentCount(commentRepository.countByPostId(postId))
                .scrapCount(scrapRepository.countByPostId(postId))
                .isScraped(currentUserId != null && scrapRepository.existsByUserIdAndPostId(currentUserId, postId))
                .isMine(currentUserId != null && post.getUser().getId().equals(currentUserId))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrl(post.getImageUrl())
                .imageName(post.getImageName())
                .comments(comments)
                .build();
    }

    public Page<PostResponseDto.PostListItemDto> searchPosts(String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchByKeyword(keyword, pageable);
        
        return posts.map(this::convertToListItemDto);
    }

    public Page<PostResponseDto.PostListItemDto> getMyPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(this::convertToListItemDto);
    }

    public Page<PostResponseDto.PostListItemDto> getPostsWithMyComments(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findCommentedPostsOrderByLatestMyComment(userId, pageable);
        return posts.map(this::convertToListItemDto);
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
                .authorProfileImage(authorProfile != null ? authorProfile.getImage() : null)
                .commentCount(commentRepository.countByPostId(post.getId()))
                .scrapCount(scrapRepository.countByPostId(post.getId()))
                .createdAt(post.getCreatedAt())
                .imageUrl(post.getImageUrl())
                .build();
    }
}