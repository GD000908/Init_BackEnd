# 1단계: 빌드 스테이지
FROM gradle:8.2.1-jdk17 AS builder
WORKDIR /app

# gradle wrapper 포함 전체 복사
COPY . .

# 실행 권한 부여
RUN chmod +x gradlew

# build
RUN ./gradlew build --no-daemon

# 2단계: 실제 실행용 이미지
FROM openjdk:17-jdk-slim
WORKDIR /app

# builder에서 jar 파일 복사
COPY --from=builder /app/build/libs/demo-0.0.1-SNAPSHOT.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
