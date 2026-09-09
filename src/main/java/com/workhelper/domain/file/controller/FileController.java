package com.workhelper.domain.file.controller;

import com.workhelper.domain.file.dto.FileResponse;
import com.workhelper.domain.file.service.FileService;
import com.workhelper.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> upload(
            @RequestParam(required = false) Long caseId,
            @RequestParam("file") MultipartFile file) {
        FileResponse response = fileService.upload(caseId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileResponse>> getFile(@PathVariable Long fileId) {
        return ResponseEntity.ok(ApiResponse.success(fileService.getFile(fileId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByCase(
            @RequestParam Long caseId) {
        return ResponseEntity.ok(ApiResponse.success(fileService.getFilesByCase(caseId)));
    }
}
