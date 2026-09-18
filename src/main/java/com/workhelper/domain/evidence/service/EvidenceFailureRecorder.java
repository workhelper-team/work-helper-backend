package com.workhelper.domain.evidence.service;

import com.workhelper.domain.evidence.entity.AnalysisStatus;
import com.workhelper.domain.evidence.repository.EvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 분석 실패 시 FAILED 상태만 별도 트랜잭션으로 즉시 커밋하기 위한 컴포넌트.
 *
 * 이유: analyzeEvidence()는 @Transactional 메서드인데, 그 안에서 예외를 던지면
 * 스프링이 해당 트랜잭션의 모든 변경사항(FAILED 상태 저장 포함)을 롤백해버림.
 * REQUIRES_NEW로 별도 트랜잭션을 만들면, 바깥 트랜잭션이 롤백되어도 이 저장은 유지됨.
 *
 * 주의: 같은 클래스 안에서 this.markAsFailed(...)처럼 자기 자신을 호출하면
 * 스프링 프록시를 안 거쳐서 @Transactional이 무시됨 — 그래서 별도 컴포넌트로 분리함.
 */
@Component
@RequiredArgsConstructor
public class EvidenceFailureRecorder {

    private final EvidenceRepository evidenceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(Long evidenceId) {
        evidenceRepository.findById(evidenceId)
                .ifPresent(evidence -> evidence.updateAnalysisResult(null, null, AnalysisStatus.FAILED));
    }
}