package com.workhelper.domain.user.entity;

import com.workhelper.domain.expert.entity.ExpertProfile;

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
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String role;

    public User(String email, String password, String name, String role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // ExpertProfile(노무사)과 1:1 양방향 연관관계 
    @OneToOne(mappedBy = "user")
    private ExpertProfile expertProfile;        

    public void changeRole(String role) {
    this.role = role;
}
}