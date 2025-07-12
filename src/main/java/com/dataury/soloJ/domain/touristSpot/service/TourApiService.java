package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.tourapi.key}")
    private String serviceKey;

    @PostConstruct
    public void init() {
        System.out.println("TourAPI 키 확인: " + serviceKey);
        System.out.println("✅ ENV SERVICE_KEY: " + System.getenv("SERVICE_KEY"));
    }


    public List<TourApiResponse.Item> fetchTouristSpots() {
        String url = "https://apis.data.go.kr/B551011/KorService2/areaBasedList2" +
                "?serviceKey=" + serviceKey +
                "&MobileApp=AppTest&MobileOS=ETC&arrange=A&areaCode=39&_type=json&numOfRows=10&pageNo=1";

        ResponseEntity<TourApiResponse> response = restTemplate.getForEntity(url, TourApiResponse.class);

        return response.getBody()
                .getResponse()
                .getBody()
                .getItems()
                .getItem();
    }
}
