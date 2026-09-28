package com.workhelper.domain.document.repository;

import com.workhelper.domain.document.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
    List<GeneratedDocument> findByLaborCase_CaseIdOrderByCreatedAtDesc(Long caseId);
    Optional<GeneratedDocument> findByDocumentIdAndLaborCase_CaseId(Long documentId, Long caseId);
}
