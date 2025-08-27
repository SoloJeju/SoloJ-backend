package com.dataury.soloJ.domain.report.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 상태 변경 요청")
public class UserStatusUpdateDto {
    
    @NotBlank(message = "상태는 필수입니다")
    @Schema(description = "변경할 상태", example = "active", allowableValues = {"active", "inactive"})
    private String status;
    
    @Schema(description = "변경 사유", example = "사용자 요청에 의한 계정 활성화")
    private String reason;
}