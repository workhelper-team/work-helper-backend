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

### Redis 세션 저장소

AWS ElastiCache for Valkey/Redis를 사용할 때 Spring Boot에는 primary endpoint의 호스트명과 포트를 환경변수로 전달합니다. 엔드포인트 문자열에서 `:6379`를 제외한 호스트명만 `REDIS_HOST`에 설정하고, 전송 암호화가 켜진 클러스터는 TLS를 활성화합니다.

```text
REDIS_HOST=<ElastiCache primary endpoint hostname>
REDIS_PORT=6379
REDIS_SSL_ENABLED=true
REDIS_PASSWORD=<AUTH token, if configured>
```

AUTH token을 설정하지 않은 클러스터에서는 `REDIS_PASSWORD`를 지정하지 않습니다. 애플리케이션이 EC2에서 실행된다면 두 리소스가 연결 가능한 VPC 안에 있어야 하며, ElastiCache 보안 그룹의 인바운드 TCP 6379 규칙은 애플리케이션 EC2의 보안 그룹만 허용해야 합니다. 세션 저장·연장은 쓰기를 수행하므로 reader endpoint가 아닌 primary endpoint를 사용합니다.

기본값은 위 ElastiCache primary endpoint와 TLS 활성화입니다. 다른 환경에서는 `REDIS_HOST`, `REDIS_PORT`, `REDIS_SSL_ENABLED` 환경변수로 덮어쓸 수 있습니다. AUTH token은 `REDIS_PASSWORD`에 넣고, 비밀번호는 저장소에 기록하지 말고 배포 환경변수 또는 비밀 관리 서비스를 사용합니다.

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
