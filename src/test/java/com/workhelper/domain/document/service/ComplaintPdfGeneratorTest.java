package com.workhelper.domain.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhelper.domain.document.dto.DocumentDetailResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintPdfGeneratorTest {
    private final ObjectMapper json = new ObjectMapper();
    private final ComplaintPdfGenerator generator = new ComplaintPdfGenerator();

    @Test
    void overlaysOfficialTemplateWithSavedValues() throws Exception {
        byte[] bytes = generator.generate(document("수정된 진정 사유"));
        assertThat(new String(bytes, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (var pdf = Loader.loadPDF(bytes)) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
            String text = new PDFTextStripper().getText(pdf);
            assertThat(text).contains("진 정 서", "홍길동", "회사", "수정된 진정 사유",
                    "1,500,000원", "서울청");
            assertThat(text).doesNotContain("2000. 1. 1.");
        }
    }

    @Test
    void nullSectionsLeaveTemplateBlank() throws Exception {
        var document = new DocumentDetailResponse(1L, 1L, "COMPLAINT", null,
                null, null, null, new DocumentDetailResponse.Content(null, null, null),
                OffsetDateTime.now(), OffsetDateTime.now());
        try (var pdf = Loader.loadPDF(generator.generate(document))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
            assertThat(new PDFTextStripper().getText(pdf)).doesNotContain("null");
        }
    }

    @Test
    void laborOfficeSuffixIsRemovedOnlyInPdf() throws Exception {
        assertOfficeDisplay("서울지방고용노동청", "서울지방");
        assertOfficeDisplay("서울남부고용노동지청", "서울남부");
        assertOfficeDisplay("서울지방", "서울지방");
        assertOfficeDisplay("서울청", "서울청");
        assertOfficeDisplay("임의기관명", "임의기관명");
        assertOfficeDisplay(null, null);
    }

    private void assertOfficeDisplay(String stored, String expected) throws Exception {
        DocumentDetailResponse base = document("사유");
        DocumentDetailResponse candidate = new DocumentDetailResponse(base.documentId(), base.caseId(),
                base.documentType(), base.title(), base.complainant(), base.respondent(), base.facts(),
                new DocumentDetailResponse.Content(base.content().claimReason(), stored,
                        base.content().totalUnpaidAmount()), base.createdAt(), base.updatedAt());
        try (var pdf = Loader.loadPDF(generator.generate(candidate))) {
            String text = new PDFTextStripper().getText(pdf);
            if (expected != null) assertThat(text).contains(expected);
            if (stored != null && !stored.equals(expected)) assertThat(text).doesNotContain(stored);
        }
        assertThat(candidate.content().targetLaborOffice()).isEqualTo(stored);
    }
    @Test
    void longReasonContinuesAcrossAppendixPagesWithoutDuplication() throws Exception {
        String reason = java.util.stream.IntStream.range(0, 240)
                .mapToObj(i -> "진정사유 고유번호" + String.format("%03d", i))
                .collect(java.util.stream.Collectors.joining("\n"));
        try (var pdf = Loader.loadPDF(generator.generate(document(reason)))) {
            assertThat(pdf.getNumberOfPages()).isGreaterThan(2);
            String text = new PDFTextStripper().getText(pdf);
            assertThat(text).contains("진정내용 별지");
            for (int i = 0; i < 240; i++) {
                String marker = "고유번호" + String.format("%03d", i);
                assertThat(text.split(marker, -1).length - 1).isEqualTo(1);
            }
        }
    }

    private DocumentDetailResponse document(String reason) throws Exception {
        return new DocumentDetailResponse(1L, 1L, "COMPLAINT", null,
                json.readTree("""
                    {"name":"홍길동","address":"서울특별시 중구","phone":"02-1234-5678",
                     "mobilePhone":"010-1234-5678","email":"test@example.com","receiveStatus":true}
                    """),
                json.readTree("""
                    {"name":"김대표","companyName":"회사","phone":"02-9999-9999",
                     "address":"서울특별시 강남구","businessType":"BUSINESS","employeeCount":"10"}
                    """),
                json.readTree("""
                    {"hireDate":"2024-01-01","resignationDate":"2025-01-01",
                     "employmentStatus":"RESIGNED","jobDescription":"사무 업무","payDay":"25일",
                     "contractType":"WRITTEN","unpaidWages":1500000}
                    """),
                new DocumentDetailResponse.Content(reason, "서울청", new BigDecimal("1500000")),
                OffsetDateTime.now(), OffsetDateTime.now());
    }
}
