package com.dataury.soloJ.domain.touristSpot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorTourSpotListResponse {
    private List<TourSpotResponse.TourSpotItemWithReview> list;
    private String nextCursor;
    private boolean hasNext;
    private int size;
}
