package com.workhelper.domain.laborcase.repository;

import com.workhelper.domain.laborcase.entity.LaborCase;
import org.springframework.data.jpa.repository.JpaRepository;


// ============================================================
// 노동 사건 Repository
// LaborCase Entity와 연결된 DB 데이터에 접근하는 객체
//
// JpaRepository를 상속하여 노동 사건의
// 저장, 조회, 수정, 삭제 등 기본적인 CRUD 기능을 제공
// ============================================================
public interface LaborCaseRepository
        extends JpaRepository<LaborCase, Long> {

    // ============================================================
    // JpaRepository에서 기본 CRUD 기능을 제공하므로
    // 별도의 메서드를 정의하지 않음
    //
    // save()    → 노동 사건 저장 및 수정
    // findAll() → 전체 노동 사건 조회
    // findById()→ 특정 노동 사건 조회
    // delete()  → 노동 사건 삭제
    // ============================================================

}