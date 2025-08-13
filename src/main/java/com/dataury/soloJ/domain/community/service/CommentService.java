package com.dataury.soloJ.domain.community.service;

import com.dataury.soloJ.domain.community.dto.CommentRequestDto;
import com.dataury.soloJ.domain.community.dto.CommentResponseDto;
import com.dataury.soloJ.domain.community.entity.Comment;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.repository.CommentRepository;
import com.dataury.soloJ.domain.community.repository.PostRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
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

    @Transactional
    public CommentResponseDto.CommentCreateResponseDto createComment(
            Long postId, 
            CommentRequestDto.CreateCommentDto request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        
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
}