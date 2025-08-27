package com.dataury.soloJ.domain.community.service;

import com.dataury.soloJ.domain.community.dto.CommentRequestDto;
import com.dataury.soloJ.domain.community.dto.CommentResponseDto;
import com.dataury.soloJ.domain.community.entity.Comment;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.repository.CommentRepository;
import com.dataury.soloJ.domain.community.repository.PostRepository;
import com.dataury.soloJ.domain.notification.service.NotificationService;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import com.dataury.soloJ.global.security.UserPenaltyChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationService notificationService;
    private final UserPenaltyChecker userPenaltyChecker;

    @Transactional
    public CommentResponseDto.CommentCreateResponseDto createComment(
            Long postId, 
            CommentRequestDto.CreateCommentDto request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 사용자 제재 상태 확인
        userPenaltyChecker.checkCommentPermission(userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .isDeleted(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 게시글 작성자가 댓글 작성자와 다른 경우에만 알림 전송
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createCommentNotification(
                    post.getUser(),
                    user.getName(),
                    postId
            );
        }

        return CommentResponseDto.CommentCreateResponseDto.builder()
                .commentId(savedComment.getId())
                .message("댓글이 성공적으로 작성되었습니다.")
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();

        boolean isAdmin = SecurityUtils.isAdmin();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        // 작성자 본인이거나 관리자인 경우에만 삭제 가능
        if (!comment.getUser().getId().equals(userId) && !isAdmin) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        comment.delete();
    }

    // ===== 관리자용 메서드 =====

    public CommentResponseDto.AdminCommentDetailDto getCommentDetailForAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        // 댓글 작성자 프로필 조회
        UserProfile commentAuthorProfile = userProfileRepository.findByUser(comment.getUser())
                .orElse(null);

        // 게시글 작성자 프로필 조회
        UserProfile postAuthorProfile = userProfileRepository.findByUser(comment.getPost().getUser())
                .orElse(null);

        // 댓글 상태 결정
        String commentStatus = "visible";
        if (comment.isDeleted()) {
            commentStatus = "deleted";
        } else if (!comment.isVisible()) {
            commentStatus = "hidden";
        }

        // 게시글 메타 정보 구성 (최소한의 맥락만)
        CommentResponseDto.PostMetaDto postMeta = CommentResponseDto.PostMetaDto.builder()
                .postId(comment.getPost().getId())
                .title(comment.getPost().getTitle())
                .authorNickname(postAuthorProfile != null ? postAuthorProfile.getNickName() : "익명")
                .authorId(comment.getPost().getUser().getId())
                .authorProfileImage(postAuthorProfile != null ? postAuthorProfile.getImageUrl() : null)
                .createdAt(comment.getPost().getCreatedAt())
                .build();

        return CommentResponseDto.AdminCommentDetailDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .originalContent(comment.getOriginalContent())
                .authorNickname(commentAuthorProfile != null ? commentAuthorProfile.getNickName() : "익명")
                .authorId(comment.getUser().getId())
                .authorProfileImage(commentAuthorProfile != null ? commentAuthorProfile.getImageUrl() : null)
                .isVisible(comment.isVisible())
                .isDeleted(comment.isDeleted())
                .status(commentStatus)
                .createdAt(comment.getCreatedAt())
                .postMeta(postMeta)
                .build();
    }
}