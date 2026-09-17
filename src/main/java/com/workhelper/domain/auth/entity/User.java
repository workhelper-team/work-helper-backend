package com.workhelper.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

/**
 * 사용자 테이블 연동
 */
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // DB 컬럼명 매핑
    private Long userId;        // 자바 컨벤션인 카멜 케이스로 변경

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}