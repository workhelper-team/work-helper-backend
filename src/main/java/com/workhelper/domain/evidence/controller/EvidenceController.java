package com.workhelper.domain.evidence.controller;

import com.workhelper.domain.evidence.dto.EvidenceAnalysisResponse;
import com.workhelper.domain.evidence.dto.EvidenceDeleteResponse;
import com.workhelper.domain.evidence.dto.EvidenceDetailResponse;
import com.workhelper.domain.evidence.dto.EvidenceSummaryResponse;
import com.workhelper.domain.evidence.dto.EvidenceUploadResponse;
import com.workhelper.domain.evidence.service.EvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/evidences")
@RequiredArgsConstructor
public class EvidenceController {

    // HTTP 요청을 실제 업무 로직으로 넘기는 서비스 객체다.
    // 컨트롤러에서는 입력값을 받고 응답을 만드는 일에 집중한다.
    private final EvidenceService evidenceService;

    /**
     * 현재는 JWT가 연결되지 않은 로컬 테스트 단계이므로 임시 사용자 ID를 사용한다.
     *
     * 실제 인증을 적용할 때는 이 값을 제거하고 SecurityContext의 인증 주체에서
     * 사용자 ID를 추출해야 한다. 운영 프로필에서 이 기본값을 사용하면 모든 요청이
     * 사용자 1의 권한으로 처리될 수 있으므로 보안상 허용하면 안 된다.
     */
    @Value("${app.security.test-user-id:1}")
    private Long testUserId;

    /**
     * 증거 파일을 저장소에 저장하고 DB에 메타데이터를 기록한다.
     *
     * 요청 형식은 JSON이 아니라 multipart/form-data이다. 파일 자체는 Service가
     * 저장소 추상화로 전달하며, 이 컨트롤러는 HTTP 입력을 애플리케이션 서비스로
     * 변환하는 역할만 담당한다.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceUploadResponse> uploadEvidence(
            @PathVariable Long caseId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description
    ) {
        // file은 JSON이 아닌 multipart/form-data의 파일 파트로 전달된다.
        // 파일 저장과 DB 저장은 서비스가 담당하므로 여기서는 그대로 전달한다.
        EvidenceUploadResponse response = evidenceService.uploadEvidence(caseId, file, description, testUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 저장된 증거를 AI 분석 서버에 전달한다.
     * 업로드 API와 분리되어 있으므로 업로드 직후 분석하지 않고 나중에 다시 요청할 수 있다.
     */
    @PostMapping("/{evidenceId}/analysis")
    public ResponseEntity<EvidenceAnalysisResponse> analyzeEvidence(
             @PathVariable Long caseId,
             @PathVariable Long evidenceId
    ) {
            // 분석 호출은 Service에서 소유권 확인, AI 서버 호출, 실패 상태 기록까지 처리한다.
         EvidenceAnalysisResponse response = evidenceService.analyzeEvidence(caseId, evidenceId, testUserId);
         return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EvidenceSummaryResponse>> getEvidences(@PathVariable Long caseId) {
        // 목록 응답에는 원문 분석 결과를 포함하지 않아 응답 크기를 줄인다.
        return ResponseEntity.ok(evidenceService.getEvidences(caseId, testUserId));
    }

    /** 원문 분석 결과를 포함한 증거 하나의 상세 정보를 반환한다. */
    @GetMapping("/{evidenceId}")
    public ResponseEntity<EvidenceDetailResponse> getEvidenceDetail(
            @PathVariable Long caseId,
            @PathVariable Long evidenceId
    ) {
        return ResponseEntity.ok(evidenceService.getEvidenceDetail(caseId, evidenceId, testUserId));
    }

    /** DB 레코드와 저장소의 실제 파일을 함께 삭제한다. */
    @DeleteMapping("/{evidenceId}")
    public ResponseEntity<EvidenceDeleteResponse> deleteEvidence(
            @PathVariable Long caseId,
            @PathVariable Long evidenceId
    ) {
        return ResponseEntity.ok(evidenceService.deleteEvidence(caseId, evidenceId, testUserId));
    }
} // EvidenceController 클래스의 끝
