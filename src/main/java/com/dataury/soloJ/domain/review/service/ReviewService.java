package com.dataury.soloJ.domain.review.service;

import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public List<ReviewResponseDto.ReviewTagResponseDto> getTagsByContentTypeId(int contentTypeId) {
        return Arrays.stream(ReviewTags.values())
                .filter(tag -> tag.getContentTypeId() == contentTypeId)
                .map(tag -> new ReviewResponseDto.ReviewTagResponseDto(tag.getCode(), tag.getDescription()))
                .collect(Collectors.toList());
    }
}
