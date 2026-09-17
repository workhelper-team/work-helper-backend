package com.workhelper.domain.evidence.entity;

import com.workhelper.domain.laborcase.entity.Case;
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
public class Evidence {

    // DB가 자동으로 발급하는 증거 식별자다. API 응답에서 파일을 다시 찾을 때 사용한다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id")
    private Long evidenceId;

    // 여러 증거가 하나의 사건에 속할 수 있으므로 다대일 관계로 연결한다.
    // LAZY는 증거를 조회할 때 사건 전체를 불필요하게 즉시 읽지 않도록 한다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private Case targetCase;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "storage_path", nullable = false, length = 500)
    // 파일 자체를 DB에 넣지 않고, 로컬 경로 또는 향후 S3 object key를 저장한다.
    private String storagePath;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    // AI 분석 결과는 JSON 구조이므로 PostgreSQL JSONB 컬럼에 저장한다.
    // 파일 내용이나 긴 추출 원문은 extractedText와 저장소 정책에 따라 별도로 관리한다.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis_result", columnDefinition = "jsonb")
    private String analysisResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 30)
    private AnalysisStatus analysisStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    public Evidence(Case targetCase, String originalName, String storagePath, String mimeType) {
        // 새 파일은 아직 AI 분석을 하지 않았으므로 항상 PENDING으로 시작한다.
        this.targetCase = targetCase;
        this.originalName = originalName;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.analysisStatus = AnalysisStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        // 생성 시각은 애플리케이션에서 자동으로 기록해 목록 정렬과 이력 표시 때 사용한다.
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public void applyAnalysisResult(String extractedText, String analysisResultJson, AnalysisStatus status) {
        // 분석 성공 시 결과를 저장하고, 실패 시 FAILED 상태만 기록하는 데도 사용한다.
        this.extractedText = extractedText;
        this.analysisResult = analysisResultJson;
        this.analysisStatus = status;
    }
}