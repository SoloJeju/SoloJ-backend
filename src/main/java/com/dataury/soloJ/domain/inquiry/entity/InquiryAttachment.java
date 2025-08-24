package com.dataury.soloJ.domain.inquiry.entity;

import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inquiry_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 500)
    private String storedFileName;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String contentType;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderNumber = 0;
}