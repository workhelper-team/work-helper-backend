package com.workhelper.domain.laborcase.repository;

import com.workhelper.domain.laborcase.entity.LaborCase;
import org.springframework.data.jpa.repository.JpaRepository;


// ============================================================
// 노동 사건 Repository
// LaborCase Entity와 연결된 DB 데이터를 관리
// 기본적인 저장, 조회, 수정, 삭제 기능을 제공
// ============================================================
public interface LaborCaseRepository
        extends JpaRepository<LaborCase, Long> {

    // JpaRepository를 상속받기 때문에
    // 별도의 메서드를 작성하지 않아도 기본 CRUD 기능 사용 가능
    //
    // 예:
    // save()      → 노동 사건 저장
    // findAll()   → 전체 사건 조회
    // findById()  → 특정 사건 조회
    // delete()    → 사건 삭제
}