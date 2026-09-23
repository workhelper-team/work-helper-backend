package com.workhelper.domain.legal.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * rag.legal_documents 테이블 매핑
 * 주의: 이 테이블은 AI팀 쪽 동기화 프로세스(국가법령정보 공동활용 OPEN API)가 채워넣는 것으로 추정.
 *       Spring Boot 쪽(우리)은 조회(READ)만 담당 — 생성/수정 로직 없음.
 */
@Entity
@Table(name = "legal_documents", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "legal_document_id")
    private Long legalDocumentId;

    // LAW / PRECEDENT / INTERPRETATION / LABOR_COMMISSION 등
    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;

    @Column(name = "source_id", nullable = false, length = 150)
    private String sourceId;

    //@Lob
    @Column(name = "title", columnDefinition = "TEXT",nullable = false)
    private String title;

   // @Lob
    @Column(name = "full_text", columnDefinition = "TEXT",nullable = false)
    private String fullText;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "synced_at", nullable = false)
    private OffsetDateTime syncedAt;
}