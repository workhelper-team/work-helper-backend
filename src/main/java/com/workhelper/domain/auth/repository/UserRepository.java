package com.workhelper.domain.auth.repository;

import com.workhelper.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 회원가입 시 이메일 중복 확인
    boolean existsByEmail(String email);

    // 로그인 시 회원 확인
    Optional<User> findByEmail(String email);
}
