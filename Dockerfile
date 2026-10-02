## 1단계 : 빌드 스테이지
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Gradle Wrapper와 설정 파일 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 라이브러리 미리 다운로드
# 소스 코드 변경 시 종속성 다운로드 스킵, 종속성 파일이 변경되면 다시 다운로드
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드 실행 (테스트는 제외)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

## 2단계 : 실행 스테이지 (가벼운 JRE 환경)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 스프링 부트 기본 포트 오픈
EXPOSE 8080

# 어플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]