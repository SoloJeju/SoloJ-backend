package com.dataury.soloJ.domain.review.entity;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column
    private String reviewText;

    @Column
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column
    @Builder.Default
    private Boolean receipt=false;

    @Column
    private String thumbnailUrl;

    @Column
    private String thumbnailName;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewTag> reviewTags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "touristSpot_id")
    private TouristSpot touristSpot;

    // 리뷰 정보 업데이트 (부분 수정 가능)
    public void updateReview(String reviewText, Difficulty difficulty, LocalDate visitDate) {
        if (reviewText != null) {
            this.reviewText = reviewText;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (visitDate != null) {
            this.visitDate = visitDate;
        }
    }

    // 썸네일 업데이트
    public void updateThumbnail(String thumbnailUrl, String thumbnailName) {
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailName = thumbnailName;
    }

    // 리뷰 태그 업데이트
    public void updateReviewTags(List<ReviewTag> newReviewTags) {
        if (this.reviewTags == null) this.reviewTags = new ArrayList<>();           // ✅ 방어
        this.reviewTags.clear();
        if (newReviewTags == null) return;                                          // ✅ null 들어와도 안전
        for (ReviewTag t : newReviewTags) {
            t.setReview(this);                                                      // 역방향 보장
            this.reviewTags.add(t);
        }
    }

    // 리뷰 이미지 업데이트
    public void updateImages(List<ReviewImage> newImages) {
        if (this.images == null) this.images = new ArrayList<>();
        this.images.clear();
        if (newImages == null) return;
        for (ReviewImage image : newImages) {
            image.setReview(this);
            this.images.add(image);
        }
    }
}
