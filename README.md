# Spring Boot API

본 프로젝트는 **Spring Boot 기반 REST API 서버**로, 사용자(User)·게시글(Post)·댓글(Comment) 도메인을 중심으로
**계층 분리(Controller–Service–Repository)**, **공통 응답/에러 처리**, **세션 기반 인증**, **보안 필터 체인**까지 포함한
서버 프로그래밍 예제입니다.

---

## 1. 프로젝트 개요

* 목적: 서버 애플리케이션의 **구조·흐름·운영 관점**을 학습/증명
* 형태: REST API 서버 (프론트엔드와 분리)
* 특징:

  * Controller / Service / Repository 명확 분리
  * Entity → Domain → DTO 매핑
  * 공통 응답 포맷(ApiResponse)
  * 공통 예외 처리(GlobalExceptionHandler)
  * 세션 기반 인증 + Security Filter

---

## 2. 기술 스택

* **Ubuntu 24.04 LTS (운영/배포 기준)**
* Java 17+
* Spring Boot
* Spring Web (REST API)
* Spring Security
* JDBC / JdbcTemplate
* MySQL
* Gradle
* (선택) Nginx – 리버스 프록시
* (선택) Redis – 세션 스토어

---

## 3. 프로젝트 구조

```text
spring/
├─ Application.java
├─ common/
│  ├─ config/        # 공통 설정
│  ├─ error/         # ErrorCode, ApiException
│  ├─ logging/       # 요청 로깅 필터
│  └─ response/      # ApiResponse 공통 응답
├─ security/
│  ├─ SecurityConfig.java
│  ├─ SessionAuthenticationFilter.java
│  ├─ LoginUser.java
│  └─ AuthController.java
├─ user/
│  ├─ UserController.java
│  ├─ UserService.java
│  ├─ UserRepository.java
│  ├─ JdbcUserRepository.java
│  ├─ UserEntity / User / dto/
├─ post/
│  ├─ PostController.java
│  ├─ PostService.java
│  ├─ PostRepository.java
│  ├─ JdbcPostRepository.java
│  ├─ PostEntity / Post / dto/
├─ comment/
│  ├─ CommentController.java
│  ├─ CommentService.java
│  ├─ CommentRepository.java
│  ├─ JdbcCommentRepository.java
│  ├─ CommentEntity / Comment / dto/
sql/
└─ schema.sql        # DB 스키마 정의
```

---

## 4. 주요 기능

### 사용자(User)

* 회원 생성 / 조회
* 세션 기반 로그인
* 인증 정보(SecurityContext) 관리

### 게시글(Post)

* 게시글 목록 / 단건 조회
* 게시글 작성

### 댓글(Comment)

* 게시글별 댓글 목록
* 댓글 작성

### 공통

* ApiResponse 기반 공통 응답 포맷
* ErrorCode 기반 예외 처리
* Filter 기반 요청 로깅

---

## 5. 인증/보안 구조

* Spring Security Filter Chain 사용
* 세션 기반 인증
* 로그인 성공 시 Session에 사용자 정보 저장
* 인증 필요 API는 SecurityContext 기반으로 접근 제어

---

## 6. 운영/배포 관점

본 프로젝트는 다음과 같은 운영 구조를 전제로 설계되었다.

* 실행 산출물: 단일 JAR
* 실행 방식: `java -jar` 또는 systemd 서비스
* 설정 관리: 코드 외부 환경 변수
* (선택) Nginx 리버스 프록시를 통한 외부 접근 제어

---

## 7. 실행 방법 (로컬)

### 7-1. MySQL 준비

MySQL이 설치되어 있지 않은 경우, 먼저 설치 후 실행한다.

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

데이터베이스 생성:

```sql
CREATE DATABASE koreanit_service
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

---

### 7-2. DB 스키마 생성

프로젝트에는 DB 스키마가 `sql/schema.sql` 파일로 포함되어 있다.
아래 명령으로 테이블을 생성한다.

```bash
mysql -u USER -p koreanit_service < sql/schema.sql
```

> `schema.sql`에는 users / posts / comments 테이블과 FK 제약조건이 포함되어 있다.     
> 상단의 DROP TABLE 구문은 **개발/실습 환경 전용**이다.   

---

### 7-3. 애플리케이션 실행

```bash
./gradlew bootRun
```

---


### 7-4. 환경 변수 설정

운영환경에서 아래 값은 **환경 변수**로 주입한다.

```text
SPRING_PROFILES_ACTIVE=prod
PORT=8000

DB_URL=jdbc:mysql://localhost:3306/koreanit_service
DB_USER=USER
DB_PASSWORD=PASSWORD

# 선택 사항 (Redis 세션 사용 시)
REDIS_HOST=localhost
REDIS_PORT=6379
```

> Redis를 사용하지 않는 경우에도 기본 세션(JSESSIONID)으로 실행 가능하다.

---

### 7-5. systemd 서비스 등록

`/etc/systemd/system/koreanit-api.service`
```
[Unit]
Description=Koreanit API Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/koreanit-api
ExecStart=/usr/bin/java -jar /opt/koreanit-api/app.jar
Restart=always
RestartSec=5
EnvironmentFile=/opt/koreanit-api/config/.env

[Install]
WantedBy=multi-user.target
```

서비스 리로드 및 실행
```bash
sudo systemctl daemon-reload
sudo systemctl enable koreanit-api
sudo systemctl restart koreanit-api
```

상태 확인
```bash
sudo systemctl status koreanit-api
```

---

## Release

- v1.0.0 (Initial Release)
  https://github.com/rstarkey1984/koreanit-server-spring/releases/tag/v1.0.0