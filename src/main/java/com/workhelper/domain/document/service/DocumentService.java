package com.workhelper.domain.document.service;

import com.workhelper.domain.document.dto.DocumentRequestDto;
import com.workhelper.domain.document.dto.DocumentResponseDto;
import com.workhelper.domain.document.entity.Document;
import com.workhelper.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;

    public List<DocumentResponseDto> getDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentResponseDto::new)
                .toList();
    }

    public DocumentResponseDto getDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 문서를 찾을 수 없습니다. ID: " + id
                        )
                );

        return new DocumentResponseDto(document);
    }

    @Transactional
    public DocumentResponseDto createDocument(DocumentRequestDto requestDto) {

        Document document = Document.builder()
                .title(requestDto.getTitle())
                .fileName(requestDto.getFileName())
                .fileUrl(requestDto.getFileUrl())
                .build();

        Document savedDocument = documentRepository.save(document);

        return new DocumentResponseDto(savedDocument);
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 문서를 찾을 수 없습니다. ID: " + id
                        )
                );

        documentRepository.delete(document);
    }
}