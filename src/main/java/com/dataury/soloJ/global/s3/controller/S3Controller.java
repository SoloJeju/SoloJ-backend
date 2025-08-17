package com.dataury.soloJ.global.s3.controller;

import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.s3.dto.S3DeleteRequestDto;
import com.dataury.soloJ.global.s3.dto.S3FileResponseDto;
import com.dataury.soloJ.global.s3.service.S3Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@Tag(name = "image S3 API", description = "image 업로드 API")
public class S3Controller {

    private final S3Service s3Service;

    /**
     * 파일 업로드
     */
    @PostMapping(value = "/upload",  consumes = "multipart/form-data")
    public ApiResponse<S3FileResponseDto> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.onSuccess(s3Service.uploadImage(file));
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteFile(@RequestBody S3DeleteRequestDto request) {
        s3Service.deleteFile(request.getFileName());
        return ApiResponse.onSuccess("Deleted: " + request.getFileName());
    }
}
