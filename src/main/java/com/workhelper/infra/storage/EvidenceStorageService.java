package com.workhelper.infra.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface EvidenceStorageService {

    String save(MultipartFile file);

    Resource load(String objectKey);
}