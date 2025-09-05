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

    private static final String BASE_URL_V2 = "https://apis.data.go.kr/B551011/KorService2";


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
    public List<TourApiResponse.ImageItem> fetchTouristSpotImages(Long contentId, int pageNo, int numOfRows) {
        int rows = Math.max(1, Math.min(numOfRows, 50)); // 안전 가드 (공식 스펙 100도 되긴 하나 지나친 낭비 방지)
        int page = Math.max(1, pageNo);

        String url = "https://apis.data.go.kr/B551011/KorService2/detailImage2"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC"
                + "&MobileApp=" + appName
                + "&_type=json"
                + "&contentId=" + contentId
                + "&imageYN=Y"
                + "&pageNo=" + page
                + "&numOfRows=" + rows;

        log.info("[TourAPI] detailImage2 page call URL={}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[TourAPI] HTTP={} bodyNull={}", response.getStatusCodeValue(), response.getBody()==null);
                return List.of();
            }
            String body = response.getBody().trim();

            if (!body.startsWith("<")) {
                JsonNode root = objectMapper.readTree(body);
                JsonNode itemNode = root.path("response").path("body").path("items").path("item");
                List<TourApiResponse.ImageItem> images = new ArrayList<>();
                if (itemNode.isArray()) {
                    for (JsonNode item : itemNode) images.add(toImageItem(item));
                } else if (itemNode.isObject()) {
                    images.add(toImageItem(itemNode));
                }
                log.info("[TourAPI] contentId={} pageNo={} rows={} items={}", contentId, page, rows, images.size());
                return images;
            }

            // XML fallback
            List<TourApiResponse.ImageItem> fallback = parseImageItemsFromXml(body);
            log.info("[TourAPI] XML fallback contentId={} pageNo={} items={}", contentId, page, fallback.size());
            return fallback;

        } catch (Exception e) {
            log.error("[TourAPI] error contentId={} pageNo={}", contentId, page, e);
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
    
    // 키워드 기반 관광지 검색
    public List<TourApiResponse.Item> searchSpotsByKeyword(String keyword, Integer areaCode, Integer contentTypeId, Integer page, Integer size) {
        try {
            // URL 인코딩 처리
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            
            StringBuilder urlBuilder = new StringBuilder(BASE_URL_V2 + "/searchKeyword2");
            urlBuilder.append("?serviceKey=").append(serviceKey);
            urlBuilder.append("&MobileOS=ETC");
            urlBuilder.append("&MobileApp=").append(appName);
            urlBuilder.append("&_type=json");
            urlBuilder.append("&keyword=").append(encodedKeyword);
            urlBuilder.append("&numOfRows=").append(size != null ? size : 20);
            urlBuilder.append("&pageNo=").append(page != null ? page + 1 : 1); // TourAPI는 1부터 시작
            urlBuilder.append("&arrange=A"); // 제목순 정렬
            urlBuilder.append("&areaCode=").append(areaCode != null ? areaCode : 39); // 기본값 제주도
            
            // contentTypeId 필터가 있으면 추가
            if (contentTypeId != null) {
                urlBuilder.append("&contentTypeId=").append(contentTypeId);
            }
            
            String url = urlBuilder.toString();
            log.info("[TourAPI] 키워드 검색 URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            
            log.debug("[TourAPI] 응답 내용: {}", responseBody);
            
            // XML인지 JSON인지 확인
            if (responseBody.trim().startsWith("<")) {
                log.warn("[TourAPI] XML 응답 받음, XML 파싱으로 전환");
                return parseXmlSearchResponse(responseBody);
            } else {
                // JSON 파싱
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode items = root.path("response").path("body").path("items").path("item");
                
                List<TourApiResponse.Item> result = new ArrayList<>();
                
                if (items.isArray()) {
                    // 배열인 경우
                    for (JsonNode item : items) {
                        TourApiResponse.Item tourItem = createTourItemFromJson(item);
                        result.add(tourItem);
                    }
                } else if (!items.isMissingNode()) {
                    // 단일 객체인 경우
                    TourApiResponse.Item tourItem = createTourItemFromJson(items);
                    result.add(tourItem);
                }
                
                return result;
            }
        } catch (Exception e) {
            log.error("[TourAPI] 키워드 검색 실패: {}", e.getMessage());
            return Collections.emptyList(); // 예외 발생 시 빈 리스트 반환 (DB 결과는 유지)
        }
    }
    
    // JSON에서 TourItem 생성 헬퍼 메서드
    private TourApiResponse.Item createTourItemFromJson(JsonNode item) {
        TourApiResponse.Item tourItem = new TourApiResponse.Item();
        tourItem.setContentid(item.path("contentid").asText());
        tourItem.setContenttypeid(item.path("contenttypeid").asText());
        tourItem.setTitle(item.path("title").asText());
        tourItem.setAddr1(item.path("addr1").asText());
        tourItem.setFirstimage(item.path("firstimage").asText());
        
        log.debug("[TourAPI] 파싱된 아이템 - 제목: {}, 주소: {}", 
                tourItem.getTitle(), tourItem.getAddr1());
        
        return tourItem;
    }
    
    // XML 응답 파싱 메서드
    private List<TourApiResponse.Item> parseXmlSearchResponse(String xmlResponse) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));
            
            NodeList itemNodes = doc.getElementsByTagName("item");
            List<TourApiResponse.Item> result = new ArrayList<>();
            
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element item = (Element) itemNodes.item(i);
                TourApiResponse.Item tourItem = new TourApiResponse.Item();
                
                tourItem.setContentid(getText(item, "contentid"));
                tourItem.setContenttypeid(getText(item, "contenttypeid"));
                tourItem.setTitle(getText(item, "title"));
                tourItem.setAddr1(getText(item, "addr1"));
                tourItem.setFirstimage(getText(item, "firstimage"));
                
                result.add(tourItem);
            }
            return result;
        } catch (Exception e) {
            log.error("[TourAPI] XML 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    // 위치 기반 관광지 조회
    public List<TourApiResponse.Item> fetchNearbySpots(Double latitude, Double longitude, Integer radius, Integer contentTypeId) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL_V2 + "/locationBasedList2");
        urlBuilder.append("?serviceKey=").append(serviceKey);
        urlBuilder.append("&MobileOS=WIN");
        urlBuilder.append("&MobileApp=").append(appName);
        urlBuilder.append("&_type=json");
        urlBuilder.append("&mapX=").append(longitude);  // 경도
        urlBuilder.append("&mapY=").append(latitude);   // 위도
        urlBuilder.append("&radius=").append(radius != null ? radius : 1000); // 기본 1km
        urlBuilder.append("&numOfRows=100");
        urlBuilder.append("&pageNo=1");
        urlBuilder.append("&arrange=E"); // 거리순 정렬
        
        // contentTypeId 필터가 있으면 추가
        if (contentTypeId != null) {
            urlBuilder.append("&contentTypeId=").append(contentTypeId);
        }
        
        String url = urlBuilder.toString();
        log.info("[TourAPI] 위치 기반 조회 URL: {}", url);
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("response").path("body").path("items").path("item");
            
            if (items.isArray()) {
                List<TourApiResponse.Item> result = new ArrayList<>();
                for (JsonNode item : items) {
                    TourApiResponse.Item tourItem = new TourApiResponse.Item();
                    tourItem.setContentid(item.path("contentid").asText());
                    tourItem.setContenttypeid(item.path("contenttypeid").asText());
                    tourItem.setTitle(item.path("title").asText());
                    tourItem.setAddr1(item.path("addr1").asText());
                    tourItem.setMapx(item.path("mapx").asText());
                    tourItem.setMapy(item.path("mapy").asText());
                    tourItem.setFirstimage(item.path("firstimage").asText());
                    tourItem.setDist(item.path("dist").asText()); // 거리 정보
                    result.add(tourItem);
                }
                return result;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[TourAPI] 위치 기반 조회 실패: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }
    }

    // 반복정보 조회 (detailInfo2)
    public List<Map<String, Object>> fetchDetailInfo(Long contentId, Long contentTypeId) {
        String url = BASE_URL_V2 + "/detailInfo2"
                + "?serviceKey=" + serviceKey
                + "&MobileApp=" + appName
                + "&MobileOS=ETC"
                + "&_type=json"
                + "&contentId=" + contentId
                + "&contentTypeId=" + contentTypeId
                + "&numOfRows=100";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode itemNode = objectMapper.readTree(response.getBody())
                    .path("response").path("body").path("items").path("item");

            List<Map<String, Object>> result = new ArrayList<>();

            if (itemNode.isArray()) {
                for (JsonNode node : itemNode) {
                    Map<String, Object> map = objectMapper.convertValue(node, new TypeReference<>() {});
                    // 빈 값은 제거
                    map = map.entrySet().stream()
                            .filter(e -> e.getValue() != null && !String.valueOf(e.getValue()).isBlank())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    result.add(map);
                }
            } else if (itemNode.isObject()) {
                Map<String, Object> map = objectMapper.convertValue(itemNode, new TypeReference<>() {});
                map = map.entrySet().stream()
                        .filter(e -> e.getValue() != null && !String.valueOf(e.getValue()).isBlank())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                result.add(map);
            }

            return result;

        } catch (Exception e) {
            log.error("[TourAPI] detailInfo2 호출 실패 contentId={}, contentTypeId={}", contentId, contentTypeId, e);
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }
    }

}
