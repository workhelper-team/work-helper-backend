package com.workhelper.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3EvidenceStorageService implements EvidenceStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final S3Client s3Client;
    private final String bucketName;

    public S3EvidenceStorageService(
            @Value("${storage.s3.bucket}") String bucketName,
            @Value("${storage.s3.region}") String region) {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
        this.bucketName = bucketName;
    }

    @Override
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("저장할 파일이 필요합니다.");
        }
        validateFile(file);

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String objectKey = "licenses/" + UUID.randomUUID()
                + (StringUtils.hasText(extension) ? "." + extension : "");

        try {
            byte[] fileBytes = Objects.requireNonNull(file.getBytes(), "파일 데이터가 필요합니다.");
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(fileBytes));
            return objectKey;
        } catch (IOException e) {
            throw new IllegalStateException("S3 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Resource load(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("파일 Object Key가 필요합니다.");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
                
                
        return new ByteArrayResource(s3Client.getObjectAsBytes(request).asByteArray());
    }

    private void validateFile(MultipartFile file) {
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