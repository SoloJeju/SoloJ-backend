package com.dataury.soloJ.domain.inquiry.repository;

import com.dataury.soloJ.domain.inquiry.entity.InquiryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryAttachmentRepository extends JpaRepository<InquiryAttachment, Long> {
    
    List<InquiryAttachment> findByInquiryIdOrderByOrderNumber(Long inquiryId);
    
    void deleteByInquiryId(Long inquiryId);
}