//Spring Data JPA 인터페이스
//evidences DB 테이블에 직접 접근하여 CRUD(증거 조회, 저장, 삭제 등) 을 실행

package com.workhelper.domain.evidence.repository;
import com.workhelper.domain.evidence.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/** Evidence와 기존 LaborCase의 관계를 기준으로 조회하는 Repository입니다. */
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    Page<Evidence> findByLaborCase_CaseId(Long caseId, Pageable pageable);

    Optional<Evidence> findByEvidenceIdAndLaborCase_CaseId(Long evidenceId, Long caseId);


}