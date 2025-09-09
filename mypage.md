# 마이페이지 설계/구현 가이드 v2 (Human Team6)

본 문서는 Spring Boot + Thymeleaf + Spring Security 기반의 마이페이지 기능 설계 지침입니다. 코드 구현 전, 설계·데이터 표준·UI 라벨링·i18n·검증 방침을 확정합니다.

## 목표와 범위
- 한 화면(Thymeleaf)에서 프로필 저장: 암 종류, 병기, 키(cm, 선택), 몸무게(kg, 선택)
- 마이페이지: 암종/병기/키/몸무게 모두 선택 입력(널 허용)
- 식단추천 폼: 암종/병기 필수, 키/몸무게 선택
- 저장 버튼 1번으로 즉시 반영(재인증·다단계 없음)
- 식단추천 폼은 DB를 수정하지 않고 해당 요청에서만 값 사용(마이페이지 값으로 프리필)

### 핵심 변경점(병기 UI 리디자인)
- 저장/전송 값(정규형): S1, S2, S3A, S3B, S4만 사용(DB/서버/검증)
- 화면은 사용자 친화 라벨(Primary) + 정확 표기(Secondary/툴팁)를 함께 제공
- “전문 표기 보기” 토글로 옵션 표시만 전환(값은 항상 정규값 유지)

## 라우팅/보안
- GET `/mypage` (화면 진입), POST `/mypage` (저장)
- 로그인 필수. 기존 `SecurityConfig` 유지(폼 로그인)
- CSRF 활성 상태: Thymeleaf 표준으로 hidden 필드 자동 포함

## DB 스키마 (user_profiles: 1:1 분리)
- 기준 테이블: `app_users(id BIGINT)` (이미 존재)
- 새 테이블: `user_profiles` (PK=FK=user_id, 1:1)
- 컬럼 및 제약
  - user_id BIGINT NOT NULL PRIMARY KEY (FK → app_users.id)
  - cancer_type ENUM('LIVER','STOMACH','COLON','BREAST','CERVICAL','LUNG') NULL
  - stage ENUM('S1','S2','S3A','S3B','S4') NULL
  - height_cm INT NULL, weight_kg INT NULL
  - updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- DDL 스니펫(create_tables.sql에 추가)
```
CREATE TABLE IF NOT EXISTS user_profiles (
  user_id BIGINT NOT NULL PRIMARY KEY,
  cancer_type ENUM('LIVER','STOMACH','COLON','BREAST','CERVICAL','LUNG') NULL,
  stage ENUM('S1','S2','S3A','S3B','S4') NULL,
  height_cm INT NULL,
  weight_kg INT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id)
    REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
- 참고: FK 타입 일치 확인(app_users.id BIGINT = user_profiles.user_id BIGINT)

## 값 표준과 라벨 전략
- 암종 표준: `CancerType` 열거형 {LIVER, STOMACH, COLON, BREAST, CERVICAL, LUNG}
- 병기 표준(정규 저장/전송 값): {S1, S2, S3A, S3B, S4}
- 화면 라벨링(Primary/Secondary/Help) 매핑
  - S1 → Primary: “초기 (1기)”, Secondary: “의학 표기: 1기”, Help: “일부에 국한된 초기 단계”
  - S2 → Primary: “2기”, Secondary: “의학 표기: 2기”, Help: “초기–중기 사이, 범위·크기 증가”
  - S3A → Primary: “중기 (3A)”, Secondary: “의학 표기: 3기A”, Help: “국소 진행 단계(일부 전이 없음)”
  - S3B → Primary: “진행기 (3B)”, Secondary: “의학 표기: 3기B”, Help: “국소 진행 단계(주변 조직/림프 침범 가능)”
  - S4 → Primary: “말기 (4기)”, Secondary: “의학 표기: 4기”, Help: “원격 전이 단계”
- 토글(표시 전환):
  - 기본(간단): “초기 (1기), 2기, 중기 (3A), 진행기 (3B), 말기 (4기)”
  - 전문: “1, 2, 3A, 3B, 4”
  - 토글은 표시만 바꾸며, select의 value는 항상 정규값 유지
- 정보 아이콘(툴팁): “표기는 간단화를 위해 제공되며, 실제 진단은 의료진 소견을 따릅니다.”

### 원칙 고정: 값-표시 분리
- select의 value는 항상 S1/S2/S3A/S3B/S4 중 하나(정규값)
- 토글은 라벨만 교체, 서버는 정규값 집합만 허용(클라이언트 조작 방지)

## 검증 규칙(서버)
- 값 집합(공통):
  - cancer_type ∈ {LIVER, STOMACH, COLON, BREAST, CERVICAL, LUNG}
  - stage ∈ {S1, S2, S3A, S3B, S4}
  - height_cm, weight_kg: null 허용, 입력 시 정수, 범위(예: 50–300, 10–400)
- 화면별 규칙:
  - 마이페이지: cancer_type, stage, height_cm, weight_kg 모두 선택 입력(널 허용). 입력된 경우에만 집합·범위 검증.
  - 식단추천 폼: cancer_type, stage 필수. 집합 검증 필수. 키/몸무게는 선택 입력.

## 화면(Thymeleaf) 설계 요약
- 템플릿: `templates/mypage/index.html`
- 프리필 규칙
  - 암종/병기/키/몸무게: `user_profiles` 있으면 값 채움, 없으면 빈칸
- 입력 위젯
  - 암종: 셀렉트(코드값 ↔ 라벨 매핑)
  - 병기: 셀렉트(option value=정규값 S1/S2/S3A/S3B/S4)
    - 라벨은 토글 상태에 따라 간단/전문 표기 전환
    - 선택되면 필드 아래 회색 작은 글씨로 Secondary 표기(예: “의학 표기: 3기A”) 표시
    - 정보 아이콘 hover 시 Help 문구 툴팁 제공
  - 키/몸무게: 숫자 입력(정수), 빈값 허용
- CSRF: `<input type="hidden" name="_csrf" value="...">` 자동 렌더링
- 저장 후: 동일 페이지 리다이렉트 + 상단 성공 배너

### i18n/카피 가이드(메시지 번들)
- 메시지 키 예시(값 중심 패턴 통일)
  - stage.label.simple.S1 = 초기 (1기)
  - stage.label.simple.S2 = 2기
  - stage.label.simple.S3A = 중기 (3A)
  - stage.label.simple.S3B = 진행기 (3B)
  - stage.label.simple.S4 = 말기 (4기)
  - stage.label.expert.S1 = 1
  - stage.label.expert.S2 = 2
  - stage.label.expert.S3A = 3A
  - stage.label.expert.S3B = 3B
  - stage.label.expert.S4 = 4
  - stage.secondary.S1 = 의학 표기: 1기
  - stage.secondary.S2 = 의학 표기: 2기
  - stage.secondary.S3A = 의학 표기: 3기A
  - stage.secondary.S3B = 의학 표기: 3기B
  - stage.secondary.S4 = 의학 표기: 4기
  - stage.help.S1 = 일부에 국한된 초기 단계
  - stage.help.S2 = 초기–중기 사이, 범위·크기 증가
  - stage.help.S3A = 국소 진행 단계(일부 전이 없음)
  - stage.help.S3B = 국소 진행 단계(주변 조직/림프 침범 가능)
  - stage.help.S4 = 원격 전이 단계
  - stage.toggle.expert = 전문 표기 보기
  - stage.toggle.simple = 간단 표기 보기
  - stage.tooltip.notice = 표기는 간단화를 위해 제공되며, 실제 진단은 의료진 소견을 따릅니다.
- 톤과 표현: 단정 지양, “일반적으로/보통/가능성이 있음” 등 완곡 표현 사용

### UI 동작 메모(표시 전환)
- 토글 상태값을 JS로 보관(`data-stage-mode='simple|pro'` 등)하고 select option 라벨만 교체
- 제출 값은 항상 S1/S2/S3A/S3B/S4 중 하나로 유지되어 백엔드 검증과 직접 호환

## 서비스/저장 흐름
1) GET `/mypage`: 로그인 사용자 ID로 `user_profiles` 조회 → ViewModel 구성
2) POST `/mypage`:
  - 서버 검증(화면별 규칙 적용 + 숫자 범위 + stage 정규값 검증)
  - `user_profiles` upsert(없으면 INSERT, 있으면 UPDATE) → `updated_at` 자동 갱신
  - 리다이렉트(`/mypage?success=1`)

## 식단추천 폼 연동
- 진입 시 마이페이지 값으로 프리필(같은 라벨 전략 적용)
- 폼에서 입력한 알레르기/증상은 해당 요청에서만 사용(DB 미저장)
- (후속) FastAPI 연동 시 매핑 규칙
  - gender: M/F/N → male/female (N은 생략 또는 별도 처리). 마이페이지엔 포함하지 않음(회원가입/별도 경로)
  - cancer_type: COLON 등 표준 코드 그대로(lowercase 변환 등은 API 스키마에 맞춤)
  - stage: 정규값을 API 형식으로 변환(예: S1→"1", S3A→"3A")
  - height_cm/weight_kg: null이면 JSON에서 생략

## 에러/로그 운영 메모
- 에러 응답 포맷: `{success:false, message:"..."}` 단일화
- 개인정보(이메일/이름) 로그 마스킹
- 타임존: 서버/DB Asia/Seoul 가정

## 마이그레이션 메모(운영 반영)
- (a) `user_profiles.cancer_type`, `user_profiles.stage` → NULL 허용으로 변경
  - 예시:
```
ALTER TABLE user_profiles 
  MODIFY cancer_type ENUM('LIVER','STOMACH','COLON','BREAST','CERVICAL','LUNG') NULL,
  MODIFY stage ENUM('S1','S2','S3A','S3B','S4') NULL;
```
- (b) 암종 코드 통일: 기존 `COLORECTAL` 데이터를 `COLON`으로 일괄 치환
```
UPDATE user_profiles SET cancer_type = 'COLON' WHERE cancer_type = 'COLORECTAL';
```
- (c) 인덱스/제약 영향 없음 확인(FK 동일, PK 동일, NOT NULL → NULL 변경만)

## 구현 체크리스트
- [ ] create_tables.sql에 `user_profiles` DDL 추가(컬럼 NULL 허용, COLON 코드) 및 RDS 반영
- [ ] 운영 마이그레이션 수행: NULL 허용 변경 + COLORECTAL→COLON 치환
- [ ] `templates/mypage/index.html` 생성(프리필 + CSRF)
- [ ] 병기 셀렉트: value=정규값, 라벨 토글(simple/expert), 선택 시 Secondary 표시, 툴팁 제공
- [ ] i18n 번들: stage.label.simple/expert/secondary/help/toggle 키 추가(패턴 통일)
- [ ] 컨트롤러/서비스: 마이페이지는 nullable 허용, 추천 폼은 필수 검증. stage 정규값 강제
- [ ] Enum/유틸: 병기 라벨 매핑 헬퍼 또는 메시지 접근자. CancerType에 COLON 반영
- [ ] 통합 테스트(MockMvc): 마이페이지 저장 성공(널 허용), 추천 폼 필수 누락 실패, 잘못된 stage 실패
- [ ] 식단추천 폼 프리필 연동(읽기만, 동일 라벨 전략)

## 선택 설계 근거
- 내부 정규값 고정(S1/S2/S3A/S3B/S4)으로 연동·통계·확장 시 안정성 확보
- 사용자 친화 라벨(초기/중기/말기 등) 제공 + 정확 표기 병행으로 이해도↑, 오해↓
- UI 전환은 표시만 변경하므로 서버 로직·DB 스키마 변경 불필요
- 향후 3C/4A 등 확장 시 라벨과 메시지 번들만 추가하면 UI 즉시 대응 가능
