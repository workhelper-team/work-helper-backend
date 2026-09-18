package com.workhelper.domain.evidence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.workhelper.domain.laborcase.entity.LaborCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "evidences", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 사용자가 업로드한 증거 파일의 메타데이터와 AI 분석 결과를 저장하는 Entity입니다.
 * 실제 파일 자체는 Storage에 저장하고, 이 Entity에는 Storage에서 다시 찾을 수 있는 Object Key만 보관합니다.
 */
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id")
    private Long evidenceId;

    // 사건 도메인은 별도 Case가 아니라 기존 LaborCase와 연결합니다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LaborCase laborCase;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    // URL이나 로컬 절대 경로가 아닌 Storage Object Key를 저장합니다.
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    // 파일 제목은 사용하지 않고 사용자가 입력한 설명만 저장합니다.
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    // FastAPI가 반환하는 구조화된 분석 결과를 JSONB로 저장합니다.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis_result", columnDefinition = "jsonb")
    private JsonNode analysisResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 30)
    private AnalysisStatus analysisStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    public Evidence(LaborCase laborCase, String originalName, String storagePath, String mimeType, String description, AnalysisStatus analysisStatus) {
        this.laborCase = laborCase;
        this.originalName = originalName;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.description = description;
        this.analysisStatus = (analysisStatus != null) ? analysisStatus : AnalysisStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    /** 분석 요청을 보냈음을 나타내는 PROCESSING 상태를 저장합니다. */
    public void updateAnalysisStatus(AnalysisStatus status) {
        this.analysisStatus = status;
    }

    /** FastAPI 분석 결과와 최종 상태(COMPLETED 또는 FAILED)를 함께 반영합니다. */
    public void updateAnalysisResult(String extractedText, JsonNode analysisResult, AnalysisStatus status) {
        this.extractedText = extractedText;
        this.analysisResult = analysisResult;
        this.analysisStatus = status;
    }
}