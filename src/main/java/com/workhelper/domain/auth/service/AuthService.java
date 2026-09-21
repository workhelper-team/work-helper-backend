package com.workhelper.domain.auth.service;

import com.workhelper.domain.auth.dto.AuthDto;
import com.workhelper.domain.user.entity.User;
import com.workhelper.domain.expert.entity.ExpertProfile;
import com.workhelper.domain.expert.repository.ExpertRepository;
import com.workhelper.domain.user.repository.UserRepository;
import com.workhelper.domain.auth.dto.LoginResponse;
import com.workhelper.global.security.jwt.JwtProvider;
import com.workhelper.infra.storage.EvidenceStorageService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EvidenceStorageService evidenceStorageService;

    public AuthService( UserRepository userRepository, 
                        ExpertRepository expertRepository, 
                        PasswordEncoder passwordEncoder,
                        JwtProvider jwtProvider,
                        EvidenceStorageService evidenceStorageService
                    ) {
        this.userRepository = userRepository;
        this.expertRepository = expertRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.evidenceStorageService = evidenceStorageService;
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
                dto.getName(),
                "GENERAL"
        );

        User savedUser = userRepository.save(user); // 

        return AuthDto.Response.builder()
                .userId(savedUser.getUserId()) // 
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role("GENERAL")
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
                dto.getName(),
                "GENERAL"
        );
        User savedUser = userRepository.save(user); // 저장된 user 객체 (id 포함) 반환 받기

        // Step 2. 환경별 저장소에 업로드하고 DB에는 Object Key만 저장
        String objectKey = evidenceStorageService.save(dto.getLicenseFile());
        
        // Step 3. ExpertProfiles 테이블에 노무사 부가 정보 및 파일 경로 저장
        ExpertProfile expertProfiles = new ExpertProfile(
                user,
                dto.getLicenseNumber(),
                dto.getOrganization(),
                objectKey
                 );

        expertRepository.save(expertProfiles);
        
        // Step 4. 응답 데이터 조립
        return AuthDto.Response.builder()
                .userId(savedUser.getUserId())
                .expertId(expertProfiles.getExpertId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role("GENERAL")
                .expertStatus(expertProfiles.getStatus()) // "PENDING"
                .build();
        }

    /**
     * 3. 로그인 (일반 회원 및 노무사 모두 이용)
     */
    public LoginResponse login(AuthDto.Login dto) {
        User user = userRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        // 노무사인지 확인
        ExpertProfile expert = expertRepository.findByUser_UserId(user.getUserId()).orElse(null);

        String role = resolveRole(user.getRole(), expert);
        String accessToken = jwtProvider.createToken(
            user.getUserId(), user.getEmail(), role);

        return new LoginResponse(
            accessToken,
            "Bearer",
            jwtProvider.getAccessTokenValidityInMilliseconds(),
            new LoginResponse.UserInfo(
                user.getUserId(), user.getEmail(), user.getName(), role));
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    //노무사인지 권한 확인
    private String resolveRole(String userRole, ExpertProfile expert) {
        if (expert != null && "APPROVED".equals(expert.getStatus())) {
            return "EXPERT";
        }
        return userRole;
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