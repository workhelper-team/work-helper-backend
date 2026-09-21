package com.workhelper.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalDiskEvidenceStorageService implements EvidenceStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final Path storageRoot;

    public LocalDiskEvidenceStorageService(
            @Value("${storage.local-root:./storage}") String storageRoot) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    @Override
    public String save(MultipartFile file) {
        return saveWithPrefix(file, "licenses");
    }

    @Override
    public String save(Long caseId, MultipartFile file) {
        if (caseId == null) {
            throw new IllegalArgumentException("caseId가 필요합니다.");
        }
        return saveWithPrefix(file, "evidences/" + caseId);
    }

    private String saveWithPrefix(MultipartFile file, String prefix) {
        validateFile(file);
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String objectKey = prefix + "/" + UUID.randomUUID()
                + (StringUtils.hasText(extension) ? "." + extension : "");
        Path target = resolve(objectKey);

        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target);
            return objectKey;
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Resource load(String objectKey) {
        Path path = resolve(objectKey);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("파일을 읽을 수 없습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("파일 경로가 올바르지 않습니다.", e);
        }
    }

    @Override
    public String getFileUrl(String objectKey) {
        return resolve(objectKey).toUri().toString();
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException e) {
            throw new IllegalStateException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    private Path resolve(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("파일 Object Key가 필요합니다.");
        }
        Path resolved = storageRoot.resolve(objectKey).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new IllegalArgumentException("유효하지 않은 파일 Object Key입니다.");
        }
        return resolved;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("저장할 파일이 필요합니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다.");
        }
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)
                && !"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException("PDF, JPG, PNG 파일만 첨부할 수 있습니다.");
        }
    }
}