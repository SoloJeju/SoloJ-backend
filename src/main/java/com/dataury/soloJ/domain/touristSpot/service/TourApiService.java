package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TourApiResponse.Item> fetchTouristSpots(Pageable pageable, TourSpotRequest.TourSpotRequestDto filterRequest) {
        String url = buildUrl(pageable, filterRequest);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();

            JsonNode root = objectMapper.readTree(body);
            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();

            // resultCode가 0000이 아니면 빈 리스트 처리
            if (!"0000".equals(resultCode)) {
                System.out.println("TourAPI 오류: " + header.path("resultMsg").asText());
                return List.of();
            }

            // items가 ""거나 null이거나 item이 없을 경우 빈 리스트
            JsonNode itemsNode = root.path("response").path("body").path("items");
            if (itemsNode.isTextual() || itemsNode.isNull() || !itemsNode.has("item")) {
                return List.of();
            }

            // 정상 응답이면 DTO로 파싱
            TourApiResponse parsed = objectMapper.readValue(body, TourApiResponse.class);
            return parsed.getResponse().getBody().getItems().getItem();

        } catch (Exception e) {
            // 예상 못 한 파싱 오류 등은 예외 던짐
            e.printStackTrace();
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }
    }

    private String buildUrl(Pageable pageable, TourSpotRequest.TourSpotRequestDto filterRequest) {
        int page = pageable.getPageNumber() + 1;
        int size = pageable.getPageSize();

        StringBuilder sb = new StringBuilder("https://apis.data.go.kr/B551011/KorService2/areaBasedList2");
        sb.append("?serviceKey=").append(serviceKey);
        sb.append("&MobileApp=AppTest");
        sb.append("&MobileOS=ETC");
        sb.append("&arrange=A");
        sb.append("&_type=json");
        sb.append("&numOfRows=").append(size);
        sb.append("&pageNo=").append(page);

        if (filterRequest.getContentTypeId() != null)
            sb.append("&contentTypeId=").append(filterRequest.getContentTypeId());
        if (filterRequest.getAreaCode() != null)
            sb.append("&areaCode=").append(filterRequest.getAreaCode());
        if (filterRequest.getSigunguCode() != null)
            sb.append("&sigunguCode=").append(filterRequest.getSigunguCode());

        return sb.toString();
    }
}
