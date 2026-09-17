package com.workhelper.domain.legal.repository;

import com.workhelper.domain.legal.entity.LegalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

        /**
         * API-LEGAL-001: 사용자 직접 검색.
         * title/full_text에 대한 단순 LIKE 검색 + sourceType 옵션 필터.
     *
         * 현재는 목록에 필요한 요약 DTO를 만들기 위해 엔티티를 조회한다.
         * full_text가 커지면 Projection으로 전환해 metadata와 원문 전체의 조회를 피해야 한다.
         * 또한 LIKE '%검색어%'는 일반 B-tree 인덱스를 활용하기 어려우므로, 데이터가 커질 때
         * pg_trgm 인덱스 또는 PostgreSQL 전문 검색(tsvector)을 검토한다.
         * 의미 기반 검색은 AI팀의 RAG/임베딩 파이프라인이 담당하고, 이 쿼리는 직접 키워드 검색용이다.
     */
    @Query("""
            SELECT d FROM LegalDocument d
            WHERE (:sourceType IS NULL OR d.sourceType = :sourceType)
              AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(d.fullText) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<LegalDocument> search(@Param("query") String query,
                                @Param("sourceType") String sourceType,
                                Pageable pageable);
}