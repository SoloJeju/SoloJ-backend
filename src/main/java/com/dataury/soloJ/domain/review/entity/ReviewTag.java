package com.dataury.soloJ.domain.review.entity;

import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_tags")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReviewTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewTags tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;
}
