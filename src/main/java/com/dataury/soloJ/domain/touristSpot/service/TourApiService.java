package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.tourapi.key}")
    private String serviceKey;

    public List<TourApiResponse.Item> fetchTouristSpots(Pageable pageable) {
        int page = pageable.getPageNumber() + 1; // Spring의 page는 0-based, TourAPI는 1-based
        int size = pageable.getPageSize();

        String url = "https://apis.data.go.kr/B551011/KorService2/areaBasedList2" +
                "?serviceKey=" + serviceKey +
                "&MobileApp=AppTest" +
                "&MobileOS=ETC" +
                "&arrange=A" +
                "&areaCode=39" +
                "&_type=json" +
                "&numOfRows=" + size +
                "&pageNo=" + page;

        ResponseEntity<TourApiResponse> response = restTemplate.getForEntity(url, TourApiResponse.class);

        return response.getBody()
                .getResponse()
                .getBody()
                .getItems()
                .getItem();
    }

}
