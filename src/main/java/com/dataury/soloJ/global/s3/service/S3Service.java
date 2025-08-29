package com.dataury.soloJ.global.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dataury.soloJ.global.s3.dto.S3FileResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * S3에 이미지 업로드
     */
    public S3FileResponseDto uploadImage(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename(); // S3 Key

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        amazonS3.putObject(new PutObjectRequest(bucket, fileName, image.getInputStream(), metadata));

        String url = getPublicUrl(fileName);

        // 파일명과 URL 따로 반환
        return new S3FileResponseDto(fileName, url);
    }


    /**
     * S3에서 이미지 삭제
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("The key must be specified when deleting an object");
        }
        amazonS3.deleteObject(bucket, fileName);
    }


    public String getPublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket, amazonS3.getRegionName(), fileName);
    }
}
