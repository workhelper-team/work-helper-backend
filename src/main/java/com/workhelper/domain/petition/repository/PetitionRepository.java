package com.workhelper.domain.petition.repository;

import com.workhelper.domain.petition.entity.Petition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetitionRepository extends JpaRepository<Petition, Long> {

    List<Petition> findByCaseEntityId(Long caseId);
}
