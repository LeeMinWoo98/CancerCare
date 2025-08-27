# 6대암 식단추천 기능 구현 명세서

## 📋 개요
CancerCare 웹 애플리케이션에 6대암(폐·간·대장·위·자궁경부·유방) 환자를 위한 맞춤형 식단추천 기능을 추가합니다.

---

## 🎯 0. 한눈에 보기 (요약)

* **목표**: 6대암 환자에게 반찬3개·국1개·밥1개·간식1개로 구성된 식단을 추천하는 기능 구현
* **핵심 원칙**: 기존 CancerCare UI/UX와 자연스럽게 통합, 중복 코드 최소화, 성능 최적화
* **접근 방식**: 
  - 공통화된 서비스 레이어로 중복 제거
  - 동적 컬럼 매핑으로 암 타입별 분기 최소화
  - 기존 민트색 테마와 일관된 디자인 적용

---

## 🗂️ 1. 범위와 산출물

### ✅ 포함 범위
- 데이터 읽기 전용 (기존 4개 테이블 활용)
- REST API 식단추천 엔드포인트 구현
- Spring Boot 서비스 레이어 구현
- 기존 디자인과 일관된 웹 UI 구현
- 개별 음식 재추천 기능

### ❌ 비포함 범위 (추후 고려)
- 음식 데이터 CRUD 관리 기능
- 사용자별 식단 저장/즐겨찾기
- 영양성분 분석 기능
- 테스트 코드 (기본 기능 구현 후 추가)

---

## 🗄️ 2. 데이터 모델 (기존 테이블 활용)

### 2.1 테이블 구조
현재 데이터베이스에 있는 4개 테이블을 그대로 활용:

```sql
-- 반찬 테이블
CREATE TABLE side_dish (
    id BIGINT PRIMARY KEY,
    sidedish_name VARCHAR(100) NOT NULL,
    Lung TINYINT(1) DEFAULT 0,
    Liver TINYINT(1) DEFAULT 0,
    Colorectal TINYINT(1) DEFAULT 0,
    Stomach TINYINT(1) DEFAULT 0,
    Cervical TINYINT(1) DEFAULT 0,
    Breast TINYINT(1) DEFAULT 0,
    info TEXT
);

-- 국 테이블 (soups)
-- 밥 테이블 (rice)  
-- 간식 테이블 (snack)
-- 동일한 컬럼 구조
```

### 2.2 공통 컬럼 규칙
- **ID**: 기본키
- **이름**: `{table}_name` 형태 (sidedish_name, soups_name 등)
- **암 타입**: `Lung`, `Liver`, `Colorectal`, `Stomach`, `Cervical`, `Breast` (1=추천, 0=비추천)
- **설명**: `info` (음식 설명 및 주의사항)

---

## 🏗️ 3. 아키텍처 설계

### 3.1 전체 구조
```
[프론트엔드] ─▶ [FoodController] ─▶ [FoodRecommendService] ─▶ [FoodRepository] ─▶ [MariaDB]
     ↑                                        ↓
[기존 UI 컴포넌트]                    [공통 추천 로직]
```

### 3.2 레이어별 역할
- **Controller**: API 엔드포인트 제공, 요청/응답 처리
- **Service**: 비즈니스 로직, 암 타입별 식단 조합 생성
- **Repository**: 데이터 액세스, 동적 쿼리 실행
- **Frontend**: 기존 민트색 테마 활용한 UI

---

## 🎨 4. 암 타입 및 매핑 설계

### 4.1 암 타입 열거형 (Java)
```java
public enum CancerType {
    LUNG("Lung", "폐암"),
    LIVER("Liver", "간암"), 
    COLORECTAL("Colorectal", "대장암"),
    STOMACH("Stomach", "위암"),
    CERVICAL("Cervical", "자궁경부암"),
    BREAST("Breast", "유방암");
    
    private final String columnName;
    private final String displayName;
}
```

### 4.2 테이블 메타데이터
```java
public enum FoodTableType {
    SIDE_DISH("side_dish", "sidedish_name", 3),
    SOUPS("soups", "soups_name", 1),
    RICE("rice", "rice_name", 1),
    SNACK("snack", "snack_name", 1);
    
    private final String tableName;
    private final String nameColumn;
    private final int recommendCount;
}
```

---

## 🔍 5. 데이터베이스 쿼리 설계

### 5.1 기본 추천 쿼리 (JdbcTemplate 사용)
```sql
SELECT id, {name_column} AS name, info
FROM {table_name}
WHERE {cancer_column} = 1
AND id NOT IN (:excludeIds)  -- 중복 방지 (옵션)
ORDER BY RAND()
LIMIT :count;
```

### 5.2 동적 쿼리 빌더 (JdbcTemplate 권장)
```java
public List<FoodItem> findRandomFoodsByType(
    FoodTableType tableType, 
    CancerType cancerType, 
    int count,
    List<Long> excludeIds
) {
    String sql = "SELECT id, " + tableType.getNameColumn() + " AS name, info " +
                 "FROM " + tableType.getTableName() +
                 " WHERE " + cancerType.getColumnName() + " = 1 " +
                 (excludeIds.isEmpty() ? "" : " AND id NOT IN (:excludeIds)") +
                 " ORDER BY RAND() LIMIT :count";
    
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("count", count)
        .addValue("excludeIds", excludeIds);
        
    return namedParameterJdbcTemplate.query(sql, params, foodItemRowMapper);
}
```

### 5.3 빈 결과 처리
```java
List<FoodItem> soupList = foodRepository.findRandomFoodsByType(
    FoodTableType.SOUPS, cancerType, 1, excludeIds
);
FoodItem soup = soupList.isEmpty() ? null : soupList.get(0);
boolean insufficient = soup == null || sideDishes.size() < 3;
```
```

---

## 🌐 6. API 설계 (단일 엔드포인트)

### 6.1 통합 식단 추천 API 
- **Endpoint**: `GET /api/food/recommend`
- **Parameters**: 
  - `cancer` (필수) - 암 타입 (LUNG, LIVER, COLORECTAL, STOMACH, CERVICAL, BREAST)
  - `foodType` (옵션) - 특정 음식 분류만 추천 (sideDish, soup, rice, snack)
  - `count` (옵션) - 추천 개수 (기본값: foodType별 기본 개수)
  - `excludeIds` (옵션) - 제외할 음식 ID 목록 (다시뽑기용)

### 6.2 API 응답 형식
**전체 식단 추천** (`foodType` 미지정):
```json
{
    "success": true,
    "data": {
        "cancerType": "LUNG",
        "cancerLabel": "폐암",
        "insufficient": false,
        "menu": {
            "sideDishes": [
                {"id": 1, "name": "두부조림", "info": "부드럽고 소화가 잘됨"},
                {"id": 5, "name": "시금치나물", "info": "철분 풍부"},
                {"id": 12, "name": "계란찜", "info": "단백질 보충"}
            ],
            "soup": {"id": 3, "name": "미역국", "info": "요오드 풍부"},
            "rice": {"id": 2, "name": "현미밥", "info": "식이섬유 풍부"}, 
            "snack": {"id": 7, "name": "플레인 요거트", "info": "유산균 함유"}
        }
    }
}
```

**부분 추천** (`foodType=soup&count=1&excludeIds=3`):
```json
{
    "success": true,
    "data": {
        "cancerType": "LUNG",
        "cancerLabel": "폐암",
        "insufficient": false,
        "items": [
            {"id": 8, "name": "된장국", "info": "단백질 보충"}
        ]
    }
}
```

**데이터 부족시** (`insufficient=true`):
```json
{
    "success": true,
    "data": {
        "cancerType": "LUNG",
        "cancerLabel": "폐암",
        "insufficient": true,
        "message": "해당 암 타입에 추천 가능한 음식이 부족합니다."
    }
}

---

## 💼 7. 서비스 로직 구현

### 7.1 개선된 추천 서비스 
```java
@Service
public class FoodRecommendService {
    
    public DietRecommendation recommendDiet(CancerType cancerType) {
        // 각 카테고리별 추천 시도
        List<FoodItem> sideDishes = foodRepository.findRandomFoodsByType(
            FoodTableType.SIDE_DISH, cancerType, 3, Collections.emptyList()
        );
        
        List<FoodItem> soupList = foodRepository.findRandomFoodsByType(
            FoodTableType.SOUPS, cancerType, 1, Collections.emptyList()
        );
        FoodItem soup = soupList.isEmpty() ? null : soupList.get(0);
        
        List<FoodItem> riceList = foodRepository.findRandomFoodsByType(
            FoodTableType.RICE, cancerType, 1, Collections.emptyList()
        );
        FoodItem rice = riceList.isEmpty() ? null : riceList.get(0);
        
        List<FoodItem> snackList = foodRepository.findRandomFoodsByType(
            FoodTableType.SNACK, cancerType, 1, Collections.emptyList()
        );
        FoodItem snack = snackList.isEmpty() ? null : snackList.get(0);
        
        // 데이터 부족 여부 체크
        boolean insufficient = soup == null || rice == null || snack == null || sideDishes.size() < 3;
        
        return new DietRecommendation(cancerType, sideDishes, soup, rice, snack, insufficient);
    }
    
    public List<FoodItem> recommendByType(CancerType cancerType, FoodTableType foodType, 
                                         int count, List<Long> excludeIds) {
        return foodRepository.findRandomFoodsByType(foodType, cancerType, count, excludeIds);
    }
}
```

### 7.2 예외 처리 및 빈 결과 대응
- NPE/IOOBE 방지를 위한 null 체크
- UI에서 부족한 카테고리 표시용 `insufficient` 플래그
- 빈 결과시 사용자 친화적 메시지 제공


## 🎨 8. UI/UX 설계

### 8.1 디자인 원칙
- **일관성**: 기존 CancerCare의 민트색(`#4ECDC4`) 테마 유지
- **접근성**: ARIA 레이블, 키보드 네비게이션 지원
- **반응형**: 모바일/태블릿/데스크톱 대응

### 8.2 화면 구성
```
[상단]
┌─────────────────────────────────────┐
│ 🍽️ 맞춤 식단 추천                     │
│ 암 종류 선택: [드롭다운 ▼] [추천받기]    │
└─────────────────────────────────────┘

[메인 콘텐츠]
┌─────┬─────┬─────┬─────┐
│반찬1 │ 국  │ 밥  │간식 │
├─────┼─────┼─────┼─────┤
│반찬2 │ 🔄  │ 🔄  │ 🔄  │  ← 개별 다시뽑기
├─────┤     │     │     │
│반찬3 │[부족]│     │     │  ← 데이터 부족시 배지 표시
└─────┴─────┴─────┴─────┘

[다시뽑기 버튼] - excludeIds 파라미터로 중복 방지
```

### 8.3 컴포넌트 재사용
- 기존 `Card`, `Button`, `Select` 컴포넌트 활용
- 기존 CSS 변수 및 클래스 재사용
- 새로운 스타일 컴포넌트 생성 최소화

---

## 📁 9. 파일 구조

```
src/main/java/org/example/
├── controller/
│   └── FoodController.java           # 식단 추천 API
├── service/
│   └── FoodRecommendService.java     # 추천 비즈니스 로직
├── repository/
│   └── FoodRepository.java           # 음식 데이터 조회
├── domain/
│   ├── CancerType.java              # 암 타입 열거형
│   ├── FoodTableType.java           # 테이블 타입 열거형
│   ├── FoodItem.java                # 음식 아이템 DTO
│   └── DietRecommendation.java      # 식단 추천 결과 DTO
└── dto/
    └── FoodRecommendRequest.java     # API 요청 DTO

src/main/resources/templates/
└── food/
    └── recommend.html               # 식단 추천 페이지

src/main/resources/static/
├── css/
│   └── food.css                    # 식단 관련 스타일
└── js/
    └── food.js                     # 식단 관련 JavaScript
```

---


## 🔧 11. 기술 스택

### 백엔드
- **Framework**: Spring Boot 3.3.4
- **Database**: MariaDB
- **Data Access**: JdbcTemplate (동적 쿼리에 최적화)
- **Build**: Gradle

### 프론트엔드  
- **Template Engine**: Thymeleaf
- **Styling**: 기존 CSS 변수 + 추가 CSS
- **JavaScript**: Vanilla JS (기존 패턴 유지)

---
