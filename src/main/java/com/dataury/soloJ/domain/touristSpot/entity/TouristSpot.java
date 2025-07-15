package com.dataury.soloJ.domain.touristSpot.entity;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TouristSpot extends BaseEntity {

    @Id
    @Column(name = "tourist_spot_id", nullable = false)
    private Long contentId; // TourAPI의 contentid 그대로 PK 사용

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long contentTypeId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private String firstImage;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(nullable = false)
    @Builder.Default
    private int activeGroupCount=0;

    // 동행방 생성 시
    public void incrementGroupCount() {
        this.activeGroupCount++;
    }

    // 동행방 종료 시
    public void decrementGroupCount() {
        if (this.activeGroupCount > 0) {
            this.activeGroupCount--;
        }
    }

    public void updateFromItem(TourApiResponse.Item item) {
        this.name = item.getTitle();
        this.contentTypeId = Long.valueOf(item.getContenttypeid());
        this.latitude = Double.parseDouble(item.getMapy());
        this.longitude = Double.parseDouble(item.getMapx());
        this.firstImage = item.getFirstimage(); // 있다면
    }

}

