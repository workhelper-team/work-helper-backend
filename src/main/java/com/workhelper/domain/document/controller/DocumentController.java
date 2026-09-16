package com.workhelper.domain.document.controller;

import com.workhelper.domain.document.dto.DocumentRequestDto;
import com.workhelper.domain.document.dto.DocumentResponseDto;
import com.workhelper.domain.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDto>> getDocuments() {
        return ResponseEntity.ok(documentService.getDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> getDocument(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @PostMapping
    public ResponseEntity<DocumentResponseDto> createDocument(
            @RequestBody DocumentRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                documentService.createDocument(requestDto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}