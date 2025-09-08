## 작업 목표

- 목적: 기존 Java 기반 음식 추천 로직을 FastAPI + LLM(확정: OpenRouter) 마이크로서비스로 대체한다.
- 합의된 전제:
  - 서비스 호스트: localhost
  - 서비스 포트: 8081
  - LLM 제공자: OpenRouter (환경변수 OPENROUTER_API_KEY 사용)

아래 문서는 Copilot(또는 자동생성 도구)이 정확한 코드를 생성하도록 요구사항(requirements)과 제약사항(constraints)을 명확·구체적으로 기술한 사양서입니다.

## 체크리스트 (요구사항)
- [ ] 요청/응답 JSON 키는 camelCase로 통일한다. (예: cancerType, generatedAt)
- [ ] FastAPI 서비스는 포트 8081로 동작한다.
- [ ] Spring에서 FastAPI 호출 시 타임아웃 6초로 설정한다.
- [ ] FastAPI 내부에서 LLM(External API) 요청 타임아웃은 30초로 설정한다.
- [ ] 응답 JSON의 metadata 필드에 `disclaimer` 문자열을 포함한다.
- [ ] 내부 호출 인증은 `X-Internal-Api-Key` 헤더로 수행한다.
- [ ] 폴백(fallback)은 `foodRepository.findFallbackRecommendation(...)`를 호출하도록 구현한다.
- [ ] 오류 응답과 폴백 동작을 명확히 한다(HTTP status, 로그, metadata.error 포함).

## API (요구사항 중심)

1) POST /recommend
- 설명: 암종(cancerType) 및 선택적 환자정보를 받아 추천 목록을 반환한다.
- 요청 요청 Content-Type: application/json
- 요청 JSON (camelCase, 필드명과 타입을 엄격히 사용):
  {
    "cancerType": "STOMACH",   // string, 필수
    "stage": "II",             // string, 선택
    "age": 62,                   // integer, 선택
    "gender": "F",             // string, 선택
    "allergies": ["egg", "peanut"], // array<string>, 선택
    "targets": { "calories": 2000 }   // object, 선택
  }

- 성공 응답 (200) JSON 예시 (모든 키는 camelCase):
  {
    "recommendations": [
      {
        "name": "Steamed fish with vegetables",
        "category": "main",
        "portion": "1 serving",
        "reason": "High-quality protein and easy digestion for post-op patients.",
        "nutrition": { "calories": 350, "proteinG": 28, "fatG": 12, "carbsG": 22 },
        "warnings": ["low fiber"]
      }
    ],
    "metadata": {
      "model": "openrouter/gpt-4o-mini",
      "promptVersion": "v1",
      "generatedAt": "2025-09-04T12:00:00Z",
      "disclaimer": "This is not medical advice. Consult a clinician for medical guidance."
    }
  }

2) GET /healthz
- 성공 응답 (200): { "status": "ok" }

3) 오류 및 폴백 규칙 (요구사항)
- FastAPI에서 내부 LLM 호출(외부 OpenRouter) 실패 또는 유효성 검사 실패 시:
  - 반환 HTTP 상태: 502 (Bad Gateway) 또는 500 (Internal Server Error) — 실패 원인에 따라 선택
  - 응답 본문에 `metadata.error` 필드 추가 (예: "llm_timeout", "validation_failed")
  - 동시에 내부적으로 로그를 남기고, 응답 대신 Spring이 폴백을 실행하도록 502를 반환(설정에 따라).
- Spring 측에서 FastAPI 호출이 실패(타임아웃 또는 5xx)하면:
  - 즉시 `foodRepository.findFallbackRecommendation(cancerType, stage, age, gender, allergies)`를 호출하여 폴백 결과를 생성
  - 최종 사용자에게는 폴백 결과를 반환하고, HTTP 응답에 `metadata.error` 또는 `metadata.fallback=true`를 포함

## 보안 및 환경변수 (요구사항)
- 필수 환경변수:
  - OPENROUTER_API_KEY (LLM 호출용)
  - INTERNAL_API_KEY (Spring ↔ FastAPI 내부 인증)
- 선택 환경변수:
  - PORT (기본 8081)
  - OPENROUTER_MODEL (권장 모델명)
  - LLM_REQUEST_TIMEOUT_SECONDS (기본 30)

Spring은 FastAPI에 요청할 때 HTTP 헤더 `X-Internal-Api-Key`에 INTERNAL_API_KEY 값을 포함해야 한다.

## 시간초(타임아웃) 정책(명확 규정)
- Spring → FastAPI 호출: 타임아웃 = 6초 (WebClient/RestTemplate의 요청 타임아웃)
- FastAPI 내부에서 OpenRouter(LLM) 호출: 타임아웃 = 30초

이 규칙을 문서화하면 자동생성 도구가 두 타임아웃을 혼동하지 않습니다.

## 응답에 disclaimer 위치 (명확 규정)
- 모든 성공 응답의 `metadata` 객체에 `disclaimer` 문자열을 포함해야 한다.

## 폴백 메서드 시그니처 (구체적 힌트)
- Java 쪽 폴백 호출 예시(명시적):
  foodRepository.findFallbackRecommendation(String cancerType, String stage, Integer age, String gender, List<String> allergies)

실제 `FoodRepository` 안에 이 시그니처가 없다면, `FoodRecommendService`에 다음과 같은 private helper를 추가하라:
  private RecommendationDTO callDbFallback(String cancerType, String stage, Integer age, String gender, List<String> allergies) { ... }

## Copilot/자동생성 대상 파일과 위치 힌트 (구체 지시)
- FastAPI: 파일들 위치 및 역할
  - `food_LLM/app/main.py` — FastAPI 앱과 라우트(`/recommend`, `/healthz`) 구현
  - `food_LLM/app/schemas.py` — Pydantic 모델(요청/응답 스키마, camelCase 유지)
  - `food_LLM/app/llm_client.py` — OpenRouter 호출, 타임아웃(30s), 응답 검증
  - `food_LLM/app/prompt_builder.py` — 프롬프트 생성기(구조화된 JSON 출력 요구)
  - `food_LLM/app/validator.py` — LLM 출력 JSON 검증기

- Java: 변경 위치
  - `src/main/java/org/example/service/FoodRecommendService.java` — 기존 추천 로직 대신 FastAPI 호출(WebClient) 추가. 타임아웃 6초, 실패 시 `foodRepository.findFallbackRecommendation(...)` 호출.

## 샘플 통신 예시 (명확한 I/O — camelCase)
- 요청:
  {
    "cancerType": "BREAST",
    "age": 55,
    "allergies": ["peanut"]
  }
- 성공 응답:
  {
    "recommendations": [ { "name": "Grilled salmon", "category": "main", "nutrition": { "calories": 280, "proteinG": 26 }, "warnings": [] } ],
    "metadata": { "model": "openrouter/gpt-4o-mini", "promptVersion": "v1", "generatedAt": "2025-09-04T12:00:00Z", "disclaimer": "This is not medical advice. Consult a clinician." }
  }

## 오류 처리과 로그 규칙 (요구사항)
- LLM 호출 실패: FastAPI는 502를 반환하고 `metadata.error`에 "llm_timeout" 등 코드를 넣는다.
- LLM가 잘못된 JSON을 반환하면: FastAPI는 500을 반환하고 `metadata.error`에 "validation_failed"를 넣는다.
- Spring은 502/5xx 응답을 받으면 즉시 DB 폴백을 호출하고, 사용자 응답의 `metadata.fallback=true`를 설정한다.

## 테스트 및 검증
- 단위 테스트: `prompt_builder`의 출력, `validator`의 스키마 검증, `llm_client`의 실패 시나리오를 mocking으로 검증.
- 통합 테스트: 로컬에서 FastAPI를 띄우고 Spring이 실제로 호출해 폴백 및 정상 흐름 검증.

## 최종 권장 지시문 (Copilot용 요약)
다음 지침을 Copilot에게 그대로 전달하세요:
1. "Implement a FastAPI app in `food_LLM/app/main.py` with POST /recommend and GET /healthz. Use Pydantic models in `schemas.py` and ensure all JSON keys are camelCase."
2. "In `app/llm_client.py`, call OpenRouter with a 30-second timeout. Validate the returned JSON against the Pydantic response model and raise an internal error if validation fails."
3. "In `FoodRecommendService.java`, implement an HTTP call to `http://localhost:8081/recommend` using WebClient with a 6-second timeout and header `X-Internal-Api-Key`. On timeout or 5xx, call `foodRepository.findFallbackRecommendation(String cancerType, String stage, Integer age, String gender, List<String> allergies)` and return that result, adding `metadata.fallback=true`."
4. "Always include `metadata.disclaimer` in successful responses."

---

원하시면 지금 바로:
- (A) FastAPI 코드(파일들)를 `food_LLM/app`에 생성하겠습니다. (테스트 포함)
- (B) `FoodRecommendService.java`를 수정하여 WebClient 호출 + 폴백을 구현하겠습니다.

하나를 선택해 주세요. 변경 후 해당 부분을 바로 구현하고 단위/통합 테스트를 실행해 검증하겠습니다.
