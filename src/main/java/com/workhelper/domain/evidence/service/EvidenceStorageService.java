package com.workhelper.domain.evidence.service;

import org.springframework.web.multipart.MultipartFile;

public interface EvidenceStorageService {

    /**
     * 파일을 저장하고 DB에 기록할 Object Key(파일명)를 반환합니다.
     */
    String store(MultipartFile file);

    /**
     * Object Key를 전달받아 실제 저장된 파일 경로/URL을 반환합니다.
     */
    String getFileUrl(String objectKey);

    /**
     * Object Key에 해당하는 실제 파일을 삭제합니다.
     */
    void delete(String objectKey);
}