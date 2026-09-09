package com.workhelper.domain.consultation.entity;

import com.workhelper.domain.user.entity.User;
import com.workhelper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 법률 상담 및 챗봇 대화 기록
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "consultations")
public class Consultation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationType type;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    public enum ConsultationType {
        CHATBOT, // AI 챗봇 상담
        HUMAN    // 전문가 상담
    }

    @Builder
    public Consultation(User user, String question, String answer,
                        ConsultationType type, String sessionId) {
        this.user = user;
        this.question = question;
        this.answer = answer;
        this.type = type != null ? type : ConsultationType.CHATBOT;
        this.sessionId = sessionId;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
    }
}
