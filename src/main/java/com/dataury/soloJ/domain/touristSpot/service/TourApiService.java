package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.detailIntro.*;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.tourapi.key}")
    private String serviceKey;

    public List<TourApiResponse.Item> fetchTouristSpots(Pageable pageable, TourSpotRequest.TourSpotRequestDto filterRequest) {
        String url = buildUrl(pageable, filterRequest);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!"0000".equals(resultCode)) return List.of();

            JsonNode itemsNode = root.path("response").path("body").path("items");
            if (itemsNode.isTextual() || itemsNode.isNull() || !itemsNode.has("item")) return List.of();

            TourApiResponse parsed = objectMapper.readValue(response.getBody(), TourApiResponse.class);
            return parsed.getResponse().getBody().getItems().getItem();

        } catch (Exception e) {
            log.error("Tour API 호출 중 에러 발생");
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }
    }

    public List<TourApiResponse.Item> fetchTouristSpotDetailCommon(Long contentId) {
        String url = buildDetailUrl(contentId);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!"0000".equals(resultCode)) return List.of();

            JsonNode itemsNode = root.path("response").path("body").path("items");
            if (itemsNode.isTextual() || itemsNode.isNull() || !itemsNode.has("item")) return List.of();

            TourApiResponse parsed = objectMapper.readValue(response.getBody(), TourApiResponse.class);
            return parsed.getResponse().getBody().getItems().getItem();

        } catch (Exception e) {
            log.error("Tour API 호출 중 에러 발생: contentId={}", contentId, e);
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }
    }

    public Map<String, Object> fetchDetailIntroAsMap(Long contentId, Long contentTypeId) {
        String url = buildDetailIntroUrl(contentId, contentTypeId);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode itemNode = objectMapper.readTree(response.getBody())
                    .path("response").path("body").path("items").path("item").get(0);

            Object dto = switch (contentTypeId.intValue()) {
                case 12 -> objectMapper.treeToValue(itemNode, DetailIntroType12Dto.class);
                case 14 -> objectMapper.treeToValue(itemNode, DetailIntroType14Dto.class);
                case 15 -> objectMapper.treeToValue(itemNode, DetailIntroType15Dto.class);
                case 25 -> objectMapper.treeToValue(itemNode, DetailIntroType25Dto.class);
                case 28 -> objectMapper.treeToValue(itemNode, DetailIntroType28Dto.class);
                case 32 -> objectMapper.treeToValue(itemNode, DetailIntroType32Dto.class);
                case 38 -> objectMapper.treeToValue(itemNode, DetailIntroType38Dto.class);
                case 39 -> objectMapper.treeToValue(itemNode, DetailIntroType39Dto.class);
                default -> null;
            };

            if (dto == null) return Collections.emptyMap();

            Map<String, Object> fullMap = objectMapper.convertValue(dto, new TypeReference<>() {});
            return fullMap.entrySet().stream()
                    .filter(e -> e.getValue() != null && !String.valueOf(e.getValue()).isBlank())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (Exception e) {
            log.error("Tour API 호출 중 에러 발생: contentId={}, contentTypeId={}", contentId, contentTypeId, e);
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }
    }

    private String buildDetailIntroUrl(Long contentId, Long contentTypeId) {
        return "https://apis.data.go.kr/B551011/KorService2/detailIntro2"
                + "?serviceKey=" + serviceKey
                + "&MobileApp=AppTest"
                + "&MobileOS=ETC"
                + "&_type=json"
                + "&contentId=" + contentId
                + "&contentTypeId=" + contentTypeId;
    }

    private String buildUrl(Pageable pageable, TourSpotRequest.TourSpotRequestDto filterRequest) {
        int page = pageable.getPageNumber() + 1;
        int size = pageable.getPageSize();

        StringBuilder sb = new StringBuilder("https://apis.data.go.kr/B551011/KorService2/areaBasedList2");
        sb.append("?serviceKey=").append(serviceKey);
        sb.append("&MobileApp=AppTest");
        sb.append("&MobileOS=ETC");
        sb.append("&_type=json");
        sb.append("&arrange=A");
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

    private String buildDetailUrl(Long contentId) {
        return "https://apis.data.go.kr/B551011/KorService2/detailCommon2"
                + "?serviceKey=" + serviceKey
                + "&MobileApp=AppTest"
                + "&MobileOS=ETC"
                + "&_type=json"
                + "&contentId=" + contentId;
    }
}
