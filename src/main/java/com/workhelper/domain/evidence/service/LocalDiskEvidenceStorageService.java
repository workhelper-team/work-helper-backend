//EvidenceStorageService 구현체로, 
// 개발(dev) 환경에서 업로드된 파일들을 서버의 로컬 디스크 지정 경로에 읽고 쓰는 역할을 담당

package com.workhelper.domain.evidence.service;

import com.workhelper.domain.evidence.exception.EvidenceExceptions.EvidenceStorageException;
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
 * 개발 환경용 로컬 디스크 저장 구현체.
 * 운영(S3) 구현은 구현전결정사항 #2 확정 후 별도 구현체(@Profile("prod"))로 추가.
 */
@Service
@Profile({"local", "dev"})
public class LocalDiskEvidenceStorageService implements EvidenceStorageService {

    @Value("${app.storage.local.base-path:./storage/evidences}")
    private String basePath;

    @Override
    public String store(MultipartFile file, Long caseId) {
        try {
            String extension = extractExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);

            Path dir = Paths.get(basePath, String.valueOf(caseId));
            Files.createDirectories(dir);

            Path target = dir.resolve(filename);
            file.transferTo(target);

            return target.toString();
        } catch (IOException e) {
            throw new EvidenceStorageException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void delete(String storagePath) {
       try {
        Files.deleteIfExists(Paths.get(storagePath));
      } catch (IOException e) {
        throw new EvidenceStorageException("파일 삭제 중 오류가 발생했습니다.", e);
      }
} 
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}