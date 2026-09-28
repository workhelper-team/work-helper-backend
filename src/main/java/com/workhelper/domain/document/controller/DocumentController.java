package com.workhelper.domain.document.controller;

import com.workhelper.domain.document.dto.DocumentDetailResponse;
import com.workhelper.domain.document.dto.DocumentSummaryResponse;
import com.workhelper.domain.document.dto.DocumentUpdateRequest;
import com.workhelper.domain.document.service.DocumentService;
import com.workhelper.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDetailResponse> create(@AuthenticationPrincipal JwtUserPrincipal principal,
                                                         @PathVariable Long caseId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(principal.getUserId(), caseId));
    }

    @GetMapping
    public List<DocumentSummaryResponse> list(@AuthenticationPrincipal JwtUserPrincipal principal,
                                              @PathVariable Long caseId) {
        return documentService.list(principal.getUserId(), caseId);
    }

    @GetMapping("/{documentId}")
    public DocumentDetailResponse get(@AuthenticationPrincipal JwtUserPrincipal principal,
                                      @PathVariable Long caseId, @PathVariable Long documentId) {
        return documentService.get(principal.getUserId(), caseId, documentId);
    }

    @PatchMapping("/{documentId}")
    public DocumentDetailResponse update(@AuthenticationPrincipal JwtUserPrincipal principal,
                                         @PathVariable Long caseId, @PathVariable Long documentId,
                                         @Valid @RequestBody DocumentUpdateRequest request) {
        return documentService.update(principal.getUserId(), caseId, documentId, request);
    }

    @GetMapping(value = "/{documentId}/file", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal JwtUserPrincipal principal,
                                           @PathVariable Long caseId, @PathVariable Long documentId) {
        byte[] pdf = documentService.getPdf(principal.getUserId(), caseId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"labor-complaint-" + documentId + ".pdf\"")
                .body(pdf);
    }
}
