package com.workhelper.domain.auth.service;

import com.workhelper.domain.auth.dto.AuthDto;
import com.workhelper.domain.auth.entity.User;
import com.workhelper.domain.auth.entity.ExpertProfiles;
import com.workhelper.domain.auth.repository.UserRepository;
import com.workhelper.domain.auth.repository.ExpertRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, ExpertRepository expertRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expertRepository = expertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 1. 일반 사용자 회원가입 (user 테이블에만 정보 INSERT)
     */
    @Transactional
    public AuthDto.Response signup(AuthDto.Signup dto) {
        validateDuplicateEmail(dto.getEmail());

        User user = new User(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getName()
        );

        User savedUser = userRepository.save(user); // 👈 savedUser로 받기

        return AuthDto.Response.builder()
                .userId(savedUser.getUserId()) // 👈 savedUser에서 ID 가져오기
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role("USER")
                .build();
    }

    /**
     * 2. 노무사 회원가입 
     * (user 테이블에 기본 정보 INSERT 후, 발급된 user_id를 이용해 ExpertProfiles에 추가 정보 INSERT)
     */
    @Transactional
    public AuthDto.Response expertSignup(AuthDto.ExpertSignup dto) {
        validateDuplicateEmail(dto.getEmail());

        // Step 1. User 테이블 저장
        User user = new User(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getName()
        );

        User savedUser = userRepository.save(user); // 저장된 user 객체 (id 포함) 반환 받기


        // 파일 저장 로직 추가 필요 26.09.16(홍은지)

        // 업로드된 파일의 이름을 문자열로 추출 (파일이 없으면 null)
        String fileName = (dto.getLicenseFile() != null && !dto.getLicenseFile().isEmpty()) 
                ? dto.getLicenseFile().getOriginalFilename() 
                : null;

        // Step 2. ExpertProfiles 테이블에 노무사 부가 정보 저장
        ExpertProfiles expertProfiles = new ExpertProfiles(
                savedUser.getUserId(),
                dto.getLicenseNumber(),
                dto.getOrganization(),
                fileName // 추출한 파일 이름 문자열 전달
        );

        expertRepository.save(expertProfiles);
        
        // Step 3. 응답 데이터 조립
        return AuthDto.Response.builder()
                .userId(savedUser.getUserId())
                .expertId(expertProfiles.getExpertId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role("EXPERT")
                .expertStatus(expertProfiles.getStatus()) // "PENDING"
                .build();
        }

    /**
     * 3. 로그인 (일반 회원 및 노무사 모두 이용)
     */
    public AuthDto.Response login(AuthDto.Login dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 노무사인지 확인
        ExpertProfiles expert = expertRepository.findByUserId(user.getUserId()).orElse(null);

        return AuthDto.Response.builder()
                .userId(user.getUserId())
                .expertId(expert != null ? expert.getExpertId() : null)
                .email(user.getEmail())
                .name(user.getName())
                .role(expert != null ? "EXPERT" : "USER")
                .expertStatus(expert != null ? expert.getStatus() : null)
                .build();
    }

    /**
     * 이메일 중복 검증 공통 메서드
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
    }
}