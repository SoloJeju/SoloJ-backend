package com.dataury.soloJ.domain.review.service;

import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOcrService {

    @Value("${google.application.credentials}") // 예: keys/vision-credentials.json
    private String credentialsPath;

    private final TouristSpotRepository touristSpotRepository;

    /**
     * 영수증 이미지에서 텍스트를 추출하고, contentId의 관광지 이름이 포함/유사한지 판정
     */
    public Boolean verifyReceipt(Long contentId, MultipartFile file) {
        try {
            List<String> lines = extractTextFromImage(file); // 줄 단위 텍스트
            TouristSpot touristSpot =  touristSpotRepository.findByContentId(contentId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

            return checkReceipt(touristSpot.getName(), lines);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("verifyReceipt failed", e);
            throw new GeneralException(ErrorStatus.IMAGE_TEXT_FAILD);
        }
    }

    /**
     * 영수증 텍스트 라인들 중 가게명이 포함/유사하면 true
     */
    public Boolean checkReceipt(String touristSpotName, List<String> extractedLines) {
        if (touristSpotName == null || touristSpotName.isBlank()) return false;
        if (extractedLines == null || extractedLines.isEmpty()) return false;

        String normTarget = normalizeName(touristSpotName);
        Set<String> targetVariants = buildNameVariants(normTarget);

        // 전체 텍스트(합본)에도 한 번에 검사
        String merged = extractedLines.stream()
                .map(this::normalizeName)
                .collect(Collectors.joining("\n"));

        if (matchesAnyVariant(merged, targetVariants)) return true;

        // 라인 단위 검사(+ 간단 유사도)
        for (String line : extractedLines) {
            String normLine = normalizeName(line);

            if (matchesAnyVariant(normLine, targetVariants)) return true;

            // 짧은 상호명의 오인식을 대비해 간단한 레벤슈타인 유사도 체크(0.85 이상)
            double sim = similarity(normLine, normTarget);
            if (sim >= 0.85) return true;
        }
        return false;
    }

    /**
     * Vision API로 문서 전체 텍스트(DOCUMENT_TEXT_DETECTION) 추출 후 줄 단위로 반환
     */
    public List<String> extractTextFromImage(MultipartFile file) throws IOException {
        try (InputStream credentialsStream = new ClassPathResource(credentialsPath).getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {
                ByteString imgBytes = ByteString.copyFrom(file.getBytes());
                Image img = Image.newBuilder().setContent(imgBytes).build();

                Feature feat = Feature.newBuilder()
                        .setType(Feature.Type.DOCUMENT_TEXT_DETECTION) // 영수증/문서에 더 적합
                        .build();

                // 한국어/영어 힌트
                ImageContext context = ImageContext.newBuilder()
                        .addLanguageHints("ko")
                        .addLanguageHints("en")
                        .build();

                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feat)
                        .setImage(img)
                        .setImageContext(context)
                        .build();

                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
                AnnotateImageResponse res = response.getResponsesList().get(0);

                if (res.hasError()) {
                    log.warn("Vision API error: {}", res.getError().getMessage());
                    throw new GeneralException(ErrorStatus.IMAGE_TEXT_FAILD);
                }

                String full = res.getFullTextAnnotation() != null
                        ? res.getFullTextAnnotation().getText()
                        : res.getTextAnnotationsList().isEmpty()
                        ? ""
                        : res.getTextAnnotationsList().get(0).getDescription();

                // 줄 단위 파싱 + 중복 제거
                return Arrays.stream(full.split("\\r?\\n"))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .distinct()
                        .limit(500) // 방어적 제한
                        .collect(Collectors.toList());
            }
        }
    }

    /* ===================== 문자열 유틸 ===================== */

    // 한글/영문/숫자만 남기고 공백·기호 제거 + 소문자 + 정규화(NFKD) + 흔한 접두/접미어 제거
    private String normalizeName(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFKD)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")                // 모든 공백 제거
                .replaceAll("[^0-9a-z가-힣]", "");     // 영문/숫자/한글 외 제거

        // 사업자/지점 표기 제거 (예: ㈜, (주), 주식회사, 본점, 지점, ○○점 등)
        n = n.replace("주식회사", "")
                .replace("㈜", "")
                .replace("(주)", "")
                .replace("본점", "")
                .replace("지점", "")
                .replace("점", ""); // ‘스타벅스무슨무슨점’ → ‘스타벅스무슨무슨’
        return n;
    }

    private Set<String> buildNameVariants(String normTarget) {
        Set<String> set = new HashSet<>();
        set.add(normTarget);

        // 단어(공백 기준) 조립 전제였으므로, 원래 이름에서 공백 제거 버전은 이미 normTarget.
        // 필요 시 추가 변형(예: 괄호 안 제거 등)을 여기서 확장 가능
        return set;
    }

    private boolean matchesAnyVariant(String haystack, Set<String> variants) {
        for (String v : variants) {
            if (haystack.contains(v)) return true;
        }
        return false;
    }

    // 간단한 레벤슈타인 기반 유사도 (1 - dist/maxLen)
    private double similarity(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        int dist = levenshtein(a, b);
        int max = Math.max(a.length(), b.length());
        return 1.0 - (double) dist / max;
    }

    private int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
