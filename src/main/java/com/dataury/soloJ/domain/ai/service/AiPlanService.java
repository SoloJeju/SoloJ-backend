package com.dataury.soloJ.domain.ai.service;

import com.dataury.soloJ.domain.plan.dto.CreatePlanAIDto;
import com.dataury.soloJ.domain.plan.dto.CreateSpotDto;
import com.dataury.soloJ.domain.plan.dto.DayPlanDto;
import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiPlanService {

    private final ChatGPTService chatGPTService;
    private final TourSpotService tourSpotService;

    public List<DayPlanDto> generate(CreatePlanAIDto requestDto) {
        String prompt = createPrompt(requestDto);
        String gptResponse = chatGPTService.generate(prompt);
        return parseAiResponse(gptResponse, requestDto.getStartDate().toLocalDate());
    }

    private String createPrompt(CreatePlanAIDto dto) {
        long days = dto.getEndDate().toLocalDate().toEpochDay() - dto.getStartDate().toLocalDate().toEpochDay() + 1;
        List<String> requiredPlaceNames = tourSpotService.findSpotNamesByContentIds(dto.getContentIds());
        String transportKorean = convertTransportTypeToKorean(dto.getTransportType());

        return new StringBuilder()
                .append("너는 최고의 제주도 여행 가이드야. 아래 조건에 맞춰 ")
                .append(days).append("일간 자동차 여행 계획을 짜줘.\n\n")
                .append("여행 기간: ").append(dto.getStartDate().toLocalDate())
                .append(" ~ ").append(dto.getEndDate().toLocalDate()).append("\n")
                .append("이동 수단: ").append(transportKorean).append("\n")
                .append("필수 포함 장소: ").append(String.join(", ", requiredPlaceNames)).append("\n\n")
                .append("✅ 출력 형식:\n")
                .append("각 날짜별로 시간대별 장소명을 정리해줘.\n")
                .append("식사, 카페, 휴식 포함하여 반드시 제주도 내 **실존하는 장소명**만 출력해줘.\n")
                .append("예를 들어 '점심 식사' 같은 일반 표현은 ❌, '미영이네식당', '산방산탄산온천', '제주신라호텔' 같은 이름만 ⭕️.\n")
                .append("**장소명은 괄호 밖**에, **괄호 안에는 해당 장소에서 할 활동 설명**을 적어줘.\n")
                .append("예) `10:00 ~ 12:00: 미영이네식당 (고기국수로 든든한 아침)`\n")
                .append("절대 '장소 ID - 장소명' 형식은 쓰지 마.\n\n")
                .append("✅ 예시:\n")
                .append("1일차\n")
                .append("10:00 ~ 12:00: 제주공항 (렌터카 인수)\n")
                .append("13:00 ~ 15:00: 가계해수욕장 (산책 및 사진 촬영)\n")
                .toString();
    }

    private String convertTransportTypeToKorean(TransportType transportType) {
        return switch (transportType) {
            case CAR -> "자동차";
            case BUS -> "버스";
            case TRAIN -> "기차";
            case TAXI -> "택시";
            case BICYCLE -> "자전거";
            case WALK -> "도보";
        };
    }

    public List<DayPlanDto> parseAiResponse(String response, LocalDate startDate) {
        List<DayPlanDto> days = new ArrayList<>();
        String[] lines = response.split("\n");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        int currentDayIndex = -1;
        LocalDate currentDate = startDate;

        for (String line : lines) {
            line = line.trim();
            if (line.isBlank()) continue;

            if (line.matches("^[0-9]+일차(\\s*\\([^)]*\\))?")) {
                currentDayIndex++;
                currentDate = startDate.plusDays(currentDayIndex); // 이건 그대로 두고
                days.add(new DayPlanDto(currentDayIndex, new ArrayList<>()));
                continue;
            }


            Matcher m = Pattern.compile("(\\d{2}:\\d{2})\\s*~\\s*(\\d{2}:\\d{2}):\\s*(.+?)\\s*\\((.*?)\\)").matcher(line);
            if (m.find()) {
                String startTime = m.group(1);
                String endTime = m.group(2);
                String title = m.group(3).trim();
                String memo = m.group(4).trim();

                if (title.contains("-")) {
                    String[] parts = title.split("-", 2);
                    if (parts.length == 2) {
                        title = parts[1].trim();
                    }
                }

                LocalDateTime arrivalDate = LocalDateTime.of(currentDate, LocalTime.parse(startTime, timeFormatter));
                LocalDateTime duringDate = LocalDateTime.of(currentDate, LocalTime.parse(endTime, timeFormatter));
                Long contentId = tourSpotService.resolveOrRegisterSpotByTitle(title);
                if (contentId != null && contentId == -1L) {
                    contentId = null;
                }
                if (currentDayIndex >= 0 && currentDayIndex < days.size()) {
                    CreateSpotDto spot = new CreateSpotDto(arrivalDate, duringDate, contentId, title, memo);
                    days.get(currentDayIndex).getSpots().add(spot);
                } else {

                }
            }
        }


        return days;
    }
}
