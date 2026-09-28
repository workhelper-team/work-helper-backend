package com.workhelper.domain.document.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.workhelper.domain.laborcase.entity.LaborCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "generated_documents", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneratedDocument {
    public static final String COMPLAINT = "COMPLAINT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LaborCase laborCase;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType = COMPLAINT;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data", nullable = false, columnDefinition = "jsonb")
    private JsonNode formData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public GeneratedDocument(LaborCase laborCase, String title, String content, JsonNode formData) {
        this.laborCase = laborCase;
        this.title = title;
        this.content = content;
        this.formData = formData;
    }

    public void update(String title, String content, JsonNode formData) {
        this.title = title;
        this.content = content;
        this.formData = formData;
        this.documentType = COMPLAINT;
    }
}
