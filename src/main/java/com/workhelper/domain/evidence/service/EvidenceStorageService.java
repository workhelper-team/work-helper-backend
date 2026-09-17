//물리적인 파일 저장/삭제 기능을 추상화한 인터페이스
//추후 로컬 저장소에서 AWS S3 등으로 저장소가 변경되어도 Service 로직의 수정 없이 저장소만 교체할 수 있도록 분리

package com.workhelper.domain.evidence.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 구현전결정사항 #2: 저장소를 로컬/S3 중 무엇으로 할지 미확정 상태이므로
 * 인터페이스로 분리해 추후 구현체만 교체 가능하도록 설계.
 */
public interface EvidenceStorageService {

    /**
     * 파일을 저장하고 storage_path 컬럼에 들어갈 경로/Key를 반환한다.
     */
    String store(MultipartFile file, Long caseId);

    //삭제 시 파일도 같이 지우기
    void delete(String storagePath);
}