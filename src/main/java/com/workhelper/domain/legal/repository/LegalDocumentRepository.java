package com.workhelper.domain.legal.repository;

import com.workhelper.domain.legal.entity.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {
}