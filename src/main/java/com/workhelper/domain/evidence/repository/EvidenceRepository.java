//Spring Data JPA 인터페이스
//evidences DB 테이블에 직접 접근하여 CRUD(증거 조회, 저장, 삭제 등) 을 실행

package com.workhelper.domain.evidence.repository;
import com.workhelper.domain.evidence.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    List<Evidence> findByTargetCase_CaseId(Long caseId);

    // 목록을 최신순으로 정렬해서 가져오기
    List<Evidence> findByTargetCase_CaseIdOrderByCreatedAtDesc(Long caseId);

    Optional<Evidence> findByEvidenceIdAndTargetCase_CaseId(Long evidenceId, Long caseId);


}