package com.dataury.soloJ.domain.report.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "콘텐츠 상태 변경 요청")
public class ContentStatusUpdateDto {
    
    @NotBlank(message = "상태는 필수입니다")
    @Schema(description = "변경할 상태", example = "hidden", allowableValues = {"visible", "hidden", "deleted"})
    private String status;
    
    @Schema(description = "변경 사유", example = "부적절한 내용으로 인한 숨김 처리")
    private String reason;
}