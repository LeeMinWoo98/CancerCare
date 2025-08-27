# CancerCare - 의료 진단 웹 애플리케이션

## 📋 프로젝트 개요

CancerCare는 MRI 영상 분석을 통한 의료 진단을 지원하는 웹 애플리케이션입니다. 사용자 인증 및 보안을 강화한 회원가입/로그인 시스템과 AI 기반 진단 기능을 제공합니다.

## 🎯 주요 기능

### 1. 회원 관리 시스템 (Security + 이메일 인증)

#### 🔐 회원가입 + 이메일 인증
- 이메일 중복 체크
- 회원가입 폼에서 이메일 입력 후 "인증번호 전송" 버튼 클릭
- Gmail SMTP를 통한 6자리 인증번호 발송
- 사용자가 인증번호 입력 후 "인증 확인" 버튼으로 검증
- 인증번호 일치 시에만 회원가입 완료 가능
- 비밀번호 해시(BCrypt) 저장
- 필수 정보: 아이디, 이름, 이메일, 비밀번호, 성별, 생년월일

#### 로그인
- 가입 완료된 사용자 로그인
- 로그인 상태 유지 기능

### 2. 보안 정책
- **허용 경로**: `/signup`, `/login`, `/css/**`, `/js/**`, `/images/**`
- **보호 경로**: 나머지 모든 경로는 인증 필요
- Spring Security 기반 인증/인가

### 3. AI 진단 기능
- MRI 영상 업로드 및 분석
- AI 모델을 통한 진단 결과 제공
- 진단 히스토리 관리

## 🚀 API 엔드포인트

### 인증 관련
```
POST /signup                   - 회원가입 (이메일 인증 포함)
POST /signup/send-code         - 이메일 인증번호 전송
POST /signup/verify-code       - 이메일 인증번호 확인
POST /login                    - 로그인
POST /logout                   - 로그아웃
```

### 진단 관련
```
GET  /diagnosis            - 진단 페이지
POST /diagnosis/upload     - MRI 영상 업로드 및 분석
GET  /diagnosis/history    - 진단 히스토리 조회
```

## 📊 성공 플로우

1. **회원가입 페이지 접속**: 사용자 정보 입력
2. **이메일 인증번호 전송**: `POST /signup/send-code` → Gmail로 6자리 인증번호 발송
3. **인증번호 입력 및 확인**: `POST /signup/verify-code` → 인증번호 일치 검증
4. **회원가입 완료**: `POST /signup` → DB에 사용자 정보 저장
5. **로그인**: `POST /login` → 로그인 성공 → 메인 페이지 리디렉션

## 🛠️ 기술 스택

### Backend
- **Java 17**: 최신 LTS 버전
- **Spring Boot 3.3.4**: 웹 애플리케이션 프레임워크
- **Spring Security**: 인증 및 보안
- **Spring Data JPA**: 데이터 접근 계층
- **MyBatis**: SQL 매핑 프레임워크

### Database
- **MariaDB**: 메인 데이터베이스
- **Hibernate**: ORM (ddl-auto=update)

### Frontend
- **Thymeleaf**: 서버 사이드 템플릿 엔진
- **HTML5/CSS3**: 반응형 웹 디자인
- **JavaScript**: 클라이언트 사이드 로직

### 외부 서비스
- **Gmail SMTP**: 이메일 발송
- **Python AI Model**: MRI 분석 (analyzer/mri_check.py)

### 빌드 도구
- **Gradle**: 빌드 및 의존성 관리

## 🗄️ 데이터베이스 설계

### 1. app_users 테이블 - 회원 기본정보
```sql
CREATE TABLE app_users (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  login_id        VARCHAR(30)  NOT NULL,   -- 아이디(로그인 ID)
  name            VARCHAR(50)  NOT NULL,   -- 이름
  email           VARCHAR(254) NOT NULL,   -- 이메일(소문자 저장 권장)
  password        VARCHAR(100) NOT NULL,   -- BCrypt 해시
  gender          ENUM('M','F','N') NOT NULL DEFAULT 'N', -- 성별
  birthdate       DATE         NOT NULL,   -- 생년월일
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_app_users_login_id  (login_id),
  UNIQUE KEY uk_app_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. email_verification_codes 테이블 - 이메일 인증번호 (임시)
```sql
CREATE TABLE email_verification_codes (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email           VARCHAR(254) NOT NULL,    -- 인증할 이메일
  code            CHAR(6)     NOT NULL,     -- 6자리 인증번호
  expires_at      DATETIME    NOT NULL,     -- 만료시간(5~10분)
  created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  KEY idx_email_created (email, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 📁 프로젝트 구조

```
src/main/java/org/example/
├─ Team6Application.java                    # 메인 애플리케이션
├─ config/
│   └─ SecurityConfig.java                  # Spring Security 설정
├─ security/
│   └─ CustomUserDetailsService.java        # 사용자 인증 서비스
├─ controller/
│   ├─ SignupController.java                # 회원가입 관련 컨트롤러
│   ├─ MainController.java                  # 메인 페이지 컨트롤러
│   └─ AnalysisController.java              # 진단 관련 컨트롤러
├─ service/
│   ├─ SignupService.java                   # 회원가입 비즈니스 로직
│   └─ EmailService.java                    # 이메일 발송 서비스
├─ domain/
│   ├─ User.java                            # 사용자 엔티티
│   └─ EmailVerificationCode.java           # 인증번호 엔티티
├─ repository/
│   ├─ UserRepository.java                  # 사용자 레포지토리
│   └─ EmailVerificationCodeRepository.java # 인증번호 레포지토리
└─ form/
    ├─ LoginForm.java                       # 로그인 폼
    └─ SignupForm.java                      # 회원가입 폼

src/main/resources/
├─ application.properties                   # 애플리케이션 설정
├─ static/
│   ├─ css/
│   │   ├─ main.css                        # 메인 스타일
│   │   ├─ chat.css                        # 채팅 스타일
│   │   └─ diag.css                        # 진단 스타일
│   └─ js/
│       └─ diag.js                         # 진단 관련 스크립트
└─ templates/
    ├─ login.html                          # 로그인 페이지
    ├─ signup.html                         # 회원가입 페이지 (이메일 인증 포함)
    ├─ main.html                           # 메인 페이지
    ├─ diagnosis.html                      # 진단 페이지
    ├─ chat.html                           # 채팅 페이지
    └─ fragments/
        └─ header.html                     # 공통 헤더
```

## ⚙️ 환경 설정

### Database 설정 (application.properties)
```properties
# MariaDB 연결 설정
spring.datasource.url=jdbc:mariadb://192.168.0.46:3306/human_team6
spring.datasource.username=root
spring.datasource.password=1111
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Gmail SMTP 설정
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_USERNAME:olion7234@gmail.com}
spring.mail.password=${GMAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# 이메일 인증 설정
app.auth.email.verification.code-length=6
app.auth.email.verification.expiry-minutes=10
app.auth.email.resend.cooldown-minutes=3
app.base-url=http://localhost:8080
```

### 환경 변수 설정
```bash
# Gmail SMTP 인증 정보
set GMAIL_USERNAME=your-email@gmail.com
set GMAIL_PASSWORD=your-app-password
```

## 🔧 빌드 및 실행

### 의존성 추가 필요 (build.gradle)
```gradle
// 이메일 발송을 위한 의존성 추가
implementation 'org.springframework.boot:spring-boot-starter-mail'

// 검증을 위한 의존성
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

### 실행 명령어
```bash
# 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun

# 또는
java -jar build/libs/human_team6-1.0-SNAPSHOT.jar
```

## 📱 UI/UX 특징

- **반응형 디자인**: 모바일/태블릿/데스크톱 지원
- **모던 디자인**: Pretendard 폰트, 그라디언트 로고
- **사용자 친화적**: 직관적인 폼 구성, 실시간 유효성 검사
- **브랜드 일관성**: CancerCare 브랜드 컬러 시스템

## 🧪 테스트 시나리오

1. **회원가입 + 이메일 인증 플로우 테스트**
   - 이메일 인증번호 전송/수신 테스트
   - 인증번호 일치/불일치 테스트
   - 인증번호 만료 테스트
2. **로그인/로그아웃 테스트**
3. **권한 기반 접근 제어 테스트**
4. **MRI 업로드 및 분석 테스트**

## 🚨 보안 고려사항

- BCrypt 패스워드 해싱
- CSRF 보호 (필요시 활성화)
- SQL Injection 방지 (JPA/MyBatis 사용)
- XSS 방지 (Thymeleaf 자동 이스케이핑)
- 세션 관리
- 이메일 인증번호를 통한 실시간 이메일 검증

## 🔮 향후 개발 계획

1. **소셜 로그인**: Google, Naver, Kakao 연동
2. **비밀번호 찾기**: 이메일을 통한 비밀번호 재설정
3. **프로필 관리**: 사용자 정보 수정
4. **진단 히스토리**: 과거 진단 결과 조회
5. **의료진 상담**: 실시간 채팅 기능
6. **알림 시스템**: 진단 결과 알림

## 👥 팀 구성

- **백엔드 개발**: 회원 관리 시스템, API 개발
- **프론트엔드 개발**: UI/UX, 반응형 웹 개발
- **AI 개발**: MRI 분석 모델 개발
- **인프라**: 서버 구축, 데이터베이스 관리

---

> **개발 참고사항**: 
> - 현재 SecurityConfig에서 모든 요청을 허용하고 있으므로, 실제 배포 전에 적절한 권한 설정이 필요합니다.
> - 이메일 인증번호는 임시 테이블에 저장되며, 인증 완료 또는 만료 시 자동 삭제됩니다.
> - 회원가입 시 이메일 인증이 필수이므로 `enabled` 필드가 불필요합니다.
