# WorkHelper Backend

WorkHelper의 Spring Boot Backend foundation입니다. 현재는 Java/Gradle/데이터베이스 설정을 정리한 단계이며, 비즈니스 API와 도메인 구현은 아직 포함하지 않습니다.

## 기술 기준

- Java 17
- Spring Boot 3.5.16
- Gradle Wrapper 8.11
- PostgreSQL 18
- Spring Web, Spring Data JPA, Spring Validation, Spring Security

DB Schema는 JPA 자동 생성이 아닌 별도 SQL/DDL로 관리합니다. Hibernate는 기존 Schema 검증만 수행하도록 `ddl-auto=validate`로 설정합니다.

## 로컬 설정

공통 설정은 `src/main/resources/application.yml`에 있으며, DB 접속 정보는 환경변수로 받습니다.

```text
DB_URL=jdbc:postgresql://localhost:5432/workhelper
DB_USERNAME=postgres
DB_PASSWORD=<local-secret>
```

로컬 profile이 필요하면 `src/main/resources/application-local.example.yml`을 `application-local.yml`로 복사하고, `SPRING_PROFILES_ACTIVE=local`을 설정합니다. 실제 `application-local.yml`은 Git에 포함하지 않습니다.

애플리케이션 실행 전 별도 DDL로 PostgreSQL Schema를 준비해야 합니다. 현재 repository에는 DDL을 생성하지 않습니다.

## 실행

Windows PowerShell:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

Unix 계열:

```bash
./gradlew clean build
./gradlew bootRun
```

## 현재 범위와 향후 구조

현재는 foundation만 구성되어 있습니다. JWT 인증/인가, Security 정책, CORS, AI Client, S3 파일 저장, Entity, Controller, Service, Repository는 기능 개발 단계에서 설계 문서를 기준으로 구현합니다.

향후 Java package는 필요한 기능부터 아래 방향으로 생성합니다.

```text
com.workhelper
├─ domain
│  ├─ auth
│  ├─ user
│  ├─ laborcase
│  ├─ consultation
│  ├─ evidence
│  ├─ document
│  ├─ expert
│  ├─ favorite
│  └─ legal
├─ global
└─ infra
```
