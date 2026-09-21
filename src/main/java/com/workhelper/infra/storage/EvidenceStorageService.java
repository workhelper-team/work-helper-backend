package com.workhelper.infra.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface EvidenceStorageService {

    String save(MultipartFile file);

    String save(Long caseId, MultipartFile file);

    Resource load(String objectKey);

    String getFileUrl(String objectKey);

    void delete(String objectKey);
}