# WorkHelper Backend

법률/행정 지원 플랫폼 **WorkHelper**의 메인 백엔드 서버입니다.
Spring Boot 3.x (Java 17) 기반이며, PostgreSQL과 연동되고 프론트엔드(React)의 요청을 처리하며,
필요 시 AI 서버(FastAPI)와 통신하는 중앙 허브 역할을 합니다.

## 아키텍처 개요

```
React (Frontend)
      │  REST / JSON
      ▼
┌────────────────────────────────────────────┐
│  work-helper-backend (Spring Boot 3.x)     │
│                                            │
│  ┌─ global/                                │
│  │   ├─ config  (Security, CORS, WebClient)│
│  │   ├─ common  (ApiResponse, BaseTime...) │
│  │   └─ error   (ErrorCode, Handler)       │
│  ├─ domain/                                │
│  │   ├─ user          회원/인증            │
│  │   ├─ case          사건 접수/상태 관리  │
│  │   ├─ consultation  법률 상담/챗봇 기록  │
│  │   ├─ petition      AI 진정서 결과 관리  │
│  │   ├─ community     게시판 CRUD          │
│  │   └─ file          증빙 서류 메타데이터 │
│  └─ infra/ai         FastAPI 통신 클라이언트│
└───────┬──────────────────────┬─────────────┘
        │ JPA                  │ WebClient (HTTP)
        ▼                      ▼
   PostgreSQL             FastAPI (AI 서버)
                          - OCR
                          - RAG 질의
                          - 진정서 생성
```

## 패키지 구조

```
com.workhelper
├── WorkHelperApplication.java
├── global
│   ├── config   (SecurityConfig, CorsConfig, WebClientConfig)
│   ├── common   (ApiResponse, BaseTimeEntity)
│   └── error    (ErrorCode, ErrorResponse, BusinessException, GlobalExceptionHandler)
├── domain
│   ├── user         (Controller / Service / Repository / entity / dto)
│   ├── cases        (Controller / Service / Repository / entity / dto)
│   ├── consultation (Controller / Service / Repository / entity / dto)
│   ├── petition     (Controller / Service / Repository / entity / dto)
│   ├── community    (Controller / Service / Repository / entity / dto)
│   └── file         (Controller / Service / Repository / entity / dto)
└── infra
    └── ai           (AiClient, AiRequestDto, AiResponseDto)
```

## 사전 요구사항

- JDK 17+
- PostgreSQL (기본: `jdbc:postgresql://localhost:5432/workhelper`)
- (선택) FastAPI AI 서버: `http://localhost:8000`

## 설정

[src/main/resources/application.yml](src/main/resources/application.yml) 에서 데이터소스와
AI 서버 엔드포인트(`ai.fastapi.base-url`)를 환경에 맞게 수정하세요.
DB 비밀번호는 환경변수 `DB_PASSWORD`로 주입할 수 있습니다.

## 빌드 & 실행

```bash
# 개발 서버 실행
./gradlew bootRun

# 빌드
./gradlew build

# 테스트
./gradlew test
```

> Gradle Wrapper(`gradlew`)가 없는 경우 로컬에 설치된 Gradle로 `gradle wrapper`를
> 한 번 실행하면 래퍼가 생성됩니다. IDE(IntelliJ/VS Code)에서 프로젝트를 열면
> 자동으로 인식되기도 합니다.
