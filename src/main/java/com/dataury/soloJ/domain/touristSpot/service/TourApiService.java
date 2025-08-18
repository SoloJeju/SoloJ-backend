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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    @Value("${spring.tourapi.app-name}")
    private String appName;
    
    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService1";


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



    public List<TourApiResponse.Item> searchTouristSpotByKeyword(String keyword) {
        try {
            String url = "https://apis.data.go.kr/B551011/KorService2/searchKeyword2"
                    + "?serviceKey=" + serviceKey
                    + "&MobileApp=" + appName
                    + "&MobileOS=ETC"
                    + "&_type=json"
                    + "&areaCode=39"
                    + "&numOfRows=5"
                    + "&keyword=" + keyword;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!"0000".equals(resultCode)) return List.of();

            JsonNode itemsNode = root.path("response").path("body").path("items");

            if (itemsNode.isTextual() || itemsNode.isNull() || !itemsNode.has("item")) {
                return List.of();  // 데이터 없음 처리
            }

            JsonNode itemArray = itemsNode.path("item");
            List<TourApiResponse.Item> result = new ArrayList<>();
            if (itemArray.isArray()) {
                for (JsonNode node : itemArray) {
                    result.add(objectMapper.treeToValue(node, TourApiResponse.Item.class));
                }
            } else {
                result.add(objectMapper.treeToValue(itemArray, TourApiResponse.Item.class));
            }

            return result;

        } catch (Exception e) {
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
                + "&MobileApp=" + appName
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
        sb.append("&MobileApp=").append(appName);
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
                + "&MobileApp="+appName
                + "&MobileOS=ETC"
                + "&_type=json"
                + "&contentId=" + contentId;
    }

    // 관광지 이미지 정보 조회 (detailImage2 사용)
    // 관광지 이미지 정보 조회 (detailImage2 사용) — 다른 요청과 동일한 방식(문자열 연결)
    public List<TourApiResponse.ImageItem> fetchTouristSpotImages(Long contentId) {
        String url = "https://apis.data.go.kr/B551011/KorService2/detailImage2"
                + "?serviceKey=" + serviceKey            // ✅ 그대로 붙이기 (재인코딩 금지)
                + "&MobileOS=ETC"
                + "&MobileApp=" + appName                // 다른 메서드들과 동일 스타일
                + "&_type=json"
                + "&contentId=" + contentId
                + "&imageYN=Y"
                + "&numOfRows=100";

        log.info("[TourAPI] detailImage2 URL={}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[TourAPI] detailImage2 HTTP={} bodyNull={}",
                        response.getStatusCodeValue(), (response.getBody() == null));
                return List.of();
            }

            String body = response.getBody().trim();

            // JSON 우선 파싱 (다른 메서드와 동일한 Jackson 파싱)
            if (!body.startsWith("<")) {
                JsonNode root = objectMapper.readTree(body);
                JsonNode itemNode = root.path("response").path("body").path("items").path("item");
                List<TourApiResponse.ImageItem> images = new ArrayList<>();
                if (itemNode.isArray()) {
                    for (JsonNode item : itemNode) images.add(toImageItem(item));
                } else if (itemNode.isObject()) {
                    images.add(toImageItem(itemNode));
                }
                log.info("[TourAPI] detailImage2 contentId={} items={}", contentId, images.size());
                return images;
            }

            // XML/HTML 응답 폴백 (이미 추가해 둔 파서 재사용)
            List<TourApiResponse.ImageItem> fallback = parseImageItemsFromXml(body);
            if (!fallback.isEmpty()) {
                log.info("[TourAPI] detailImage2 XML fallback used. contentId={} items={}", contentId, fallback.size());
                return fallback;
            } else {
                log.warn("[TourAPI] detailImage2 returned non-JSON and no items parsed. snippet={}",
                        body.substring(0, Math.min(200, body.length())).replaceAll("\\s+", " "));
                return List.of();
            }

        } catch (Exception e) {
            log.error("[TourAPI] detailImage2 error contentId={}", contentId, e);
            return List.of();
        }
    }



    // --- XML 폴백 파서 (JDK만 사용) ---
    private List<TourApiResponse.ImageItem> parseImageItemsFromXml(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            // resultCode 로그
            NodeList headerCodes = doc.getElementsByTagName("resultCode");
            if (headerCodes != null && headerCodes.getLength() > 0) {
                String rc = headerCodes.item(0).getTextContent();
                NodeList rms = doc.getElementsByTagName("resultMsg");
                String rm = (rms != null && rms.getLength() > 0) ? rms.item(0).getTextContent() : null;
                if (!"0000".equals(rc)) log.warn("[TourAPI] XML resultCode={} resultMsg={}", rc, rm);
            }

            NodeList items = doc.getElementsByTagName("item");
            List<TourApiResponse.ImageItem> images = new ArrayList<>();
            for (int i = 0; i < items.getLength(); i++) {
                Element el = (Element) items.item(i);   // ✅ Element 사용 (위 import 추가했음)
                TourApiResponse.ImageItem image = new TourApiResponse.ImageItem();
                image.setOriginimgurl(getText(el, "originimgurl"));
                image.setSmallimageurl(getText(el, "smallimageurl"));
                image.setImgname(getText(el, "imgname"));
                image.setSerialno(getText(el, "serialnum")); // 필드명 주의
                image.setCpyrhtDivCd(getText(el, "cpyrhtDivCd"));
                if (image.getOriginimgurl() != null && !image.getOriginimgurl().isBlank()) {
                    images.add(image);
                }
            }
            return images;
        } catch (Exception ex) {
            log.warn("[TourAPI] XML fallback parse failed: {}", ex.toString());
            return List.of();
        }
    }

    private String getText(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl != null && nl.getLength() > 0) ? nl.item(0).getTextContent() : null;
    }

    // JSON helper 그대로 유지
    private TourApiResponse.ImageItem toImageItem(JsonNode item) {
        TourApiResponse.ImageItem image = new TourApiResponse.ImageItem();
        image.setOriginimgurl(item.path("originimgurl").asText(null));
        image.setSmallimageurl(item.path("smallimageurl").asText(null));
        image.setImgname(item.path("imgname").asText(null));
        image.setSerialno(item.path("serialnum").asText(null));
        image.setCpyrhtDivCd(item.path("cpyrhtDivCd").asText(null));
        return image;
    }
}
