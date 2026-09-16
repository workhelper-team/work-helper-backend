package com.workhelper.domain.consultation.entity;

import com.workhelper.domain.laborcase.entity.LaborCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_case_id", nullable = false)
    private LaborCase laborCase;

    @Column(name = "sender_type", nullable = false)
    private String senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "structured_result", columnDefinition = "TEXT")
    private String structuredResult;

    @Builder
    public Consultation(LaborCase laborCase, String senderType, String content, String structuredResult) {
        this.laborCase = laborCase;
        this.senderType = senderType;
        this.content = content;
        this.structuredResult = structuredResult;
    }
}