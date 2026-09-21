package com.workhelper.infra.storage;

import com.workhelper.domain.evidence.service.EvidenceStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 개발 환경에서 Evidence 파일을 로컬 디스크에 저장하는 구현체입니다.
 * EvidenceService가 의존하는 domain 인터페이스를 구현하지만, 실제 파일 I/O는 infra에서 담당합니다.
 * DB에는 이 클래스가 반환하는 Object Key만 저장하고 전체 경로는 저장하지 않습니다.
 */
@Service
@Profile({"local", "dev"})
public class LocalDiskEvidenceStorageService implements EvidenceStorageService {

    @Value("${app.storage.local.base-path:./storage/evidences}")
    private String basePath;

    @Override
    public String store(Long caseId, MultipartFile file) {
        try {
            String extension = extensionForMimeType(file.getContentType());
            String objectKey = "evidences/" + caseId + "/" + UUID.randomUUID() + "." + extension;

            Path dir = Paths.get(basePath, "evidences", String.valueOf(caseId));
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path target = Paths.get(basePath, objectKey);
            file.transferTo(target.toFile());

            // 호출자에게는 DB에 저장할 Object Key만 반환합니다.
            return objectKey;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String getFileUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        // 개발 환경에서는 로컬 절대 경로를 반환하며, 운영 S3 구현체는 URL을 반환하게 됩니다.
        return Paths.get(basePath, objectKey).toAbsolutePath().toString();
    }

    @Override
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            Path target = Paths.get(basePath, objectKey);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    private String extensionForMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new IllegalArgumentException("지원하지 않는 이미지 MIME 타입입니다.");
        };
    }
}