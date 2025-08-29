package com.dataury.soloJ.domain.touristSpot.controller;

import com.dataury.soloJ.domain.touristSpot.service.TouristSpotBatchService;
import com.dataury.soloJ.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotBatchController {
    
    private final TouristSpotBatchService batchService;
    
    @PostMapping("/update-average-ratings")
    public ApiResponse<String> updateAllAverageRatings() {
        batchService.updateAllAverageRatings();
        return ApiResponse.onSuccess("모든 관광지의 평균 평점이 업데이트되었습니다.");
    }
}