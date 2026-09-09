package com.workhelper.domain.file.service;

import com.workhelper.domain.cases.entity.Case;
import com.workhelper.domain.cases.repository.CaseRepository;
import com.workhelper.domain.file.dto.FileResponse;
import com.workhelper.domain.file.entity.FileMetadata;
import com.workhelper.domain.file.repository.FileMetadataRepository;
import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final CaseRepository caseRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public FileResponse upload(Long caseId, MultipartFile file) {
        Case caseEntity = null;
        if (caseId != null) {
            caseEntity = caseRepository.findById(caseId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));
        }

        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir).resolve(storedFileName);

        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException e) {
            log.error("File save failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.");
        }

        FileMetadata metadata = FileMetadata.builder()
                .caseEntity(caseEntity)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .filePath(targetPath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return FileResponse.from(fileMetadataRepository.save(metadata));
    }

    public FileResponse getFile(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
        return FileResponse.from(metadata);
    }

    public List<FileResponse> getFilesByCase(Long caseId) {
        return fileMetadataRepository.findByCaseEntityId(caseId).stream()
                .map(FileResponse::from)
                .toList();
    }
}
