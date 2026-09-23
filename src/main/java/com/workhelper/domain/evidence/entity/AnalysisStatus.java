//증거 분석의 처리 상태를 나타내는 Enum 클래스
package com.workhelper.domain.evidence.entity;

/**
 * evidences.analysis_status 컬럼 값
 * DB Default: PENDING
 */
public enum AnalysisStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}