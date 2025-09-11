![우리팀 로고](./src/main/resources/static/img/pureCancerCareAI.png)
> "AI로 당신의 건강을 예측하고 챗봇으로 당신의 일상을 함께하는, 가장 스마트한 건강 동반자"

## ✨ 프로젝트 소개 (Introduction)
CancerCare는 CNN 딥러닝 기반의 암 예측과 AI 챗봇을 통해 환자별 맞춤 상담, 식단 및 주변 병원 정보를 제공하는 지능형 헬스케어 플랫폼입니다. 복잡하고 흩어져 있는 의료 정보를 하나로 모아, 암 환자분들의 막막한 여정에 든든한 길잡이가 되어 드립니다.

#### (1)프로젝트 기간
기간: 2025.08.11 ~ 2025.09.18(총 39일, 5주 4일)

## 🚀 추진 배경 및 기대효과
#### 추진 배경
- 고령화로 인한 암 발병률 증가: 65세 이상 인구가 늘면서 암 환자 수가 지속 상승하고, 조기 발견·지속 관리의 중요성이 커지고 있습니다.
- 의료 접근성 한계: 병원 방문에 따른 비용·시간 부담과 지역 격차로 적시에 의료 서비스를 이용하기 어려운 환경이 존재합니다.
- 전문의 부족·시스템 과부하: 암 전문의 수요 대비 공급 불균형으로 대기 시간이 길고, 의료 시스템 과부하로 효율적 진료 제공이 어렵습니다.
- 파편화된 암 관리: 진단, 상담, 생활관리, 병원 연계가 분절적으로 제공되어 환자 경험과 연속성이 저하됩니다.

#### 기대 효과
- 조기 발견율 향상: AI 기반 영상 분석으로 조기 진단을 지원해 치료 개입 시점을 앞당기고 생존율 제고에 기여합니다.
- 의료비 절감·효율 증대: 조기 발견으로 치료비를 낮추고, 불필요한 병원 방문을 줄여 시간·경제적 비용을 절약합니다.
- 개인 맞춤형 통합 케어: 진단부터 생활관리까지 원스톱 제공으로 편의를 높이고, 개인별 식단·습관 관리로 치료 효과를 증진합니다.
- 의료 접근성 개선: 지역 제약 없이 AI 상담을 제공하고, 위치 기반 병원 연계로 신속한 치료 연결을 돕습니다.

## 🚀 주요 기능 (Features)

* **🔬 AI 암 예측**: CNN 딥러닝 모델인 YOLO V8을 사용하여 의료 이미지를 기반으로 암 발병 가능성을 예측합니다.
![이미지 진단](./src/main/resources/static/img/CancerCariChatbot2.png)
* **💬 AI 챗봇 상담**: 생활 습관, 식단, 일반적인 궁금증에 대해 24시간 맞춤형 상담을 제공합니다.
![챗봇](./src/main/resources/static/img/CancerCareChat2.JPG)
* **🥗 맞춤 식단 추천**: 환자의 상태를 고려한 건강하고 균형 잡힌 식단을 추천합니다.
![식단](./src/main/resources/static/img/CancerFood.JPG)
* **🏥 주변 병원 찾기**: Kakao Map API를 활용하여 현재 위치를 기준으로 가장 가까운 병원을 안내합니다.
![병원](./src/main/resources/static/img/CancaerHospital.JPG)

### 🛠️ 기술 스택 (Tech Stack)

#### **Backend**
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-security)
[![JPA](https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white)](https://jakarta.ee/specifications/persistence/)
[![RestAPI](https://img.shields.io/badge/RestAPI-000000?style=for-the-badge)](https://restfulapi.net/)

#### **Frontend**
[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/Guide/HTML/HTML5)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)

#### **AI / ML**
[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PyTorch](https://img.shields.io/badge/PyTorch-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white)](https://pytorch.org/)
[![YOLOv8](https://img.shields.io/badge/YOLOv8-00467F?style=for-the-badge)](https://github.com/ultralytics/ultralytics)
[![Langchain](https://img.shields.io/badge/Langchain-000000?style=for-the-badge)](https://www.langchain.com/)

#### **Database**
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)

#### **인프라/클라우드(Infrastructure/Cloud)**
[![AWS](https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/)

#### **빌드 도구 (Build Tool)**
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

#### **외부 API (External APIs)**
[![Kakao Map API](https://img.shields.io/badge/Kakao_Map_API-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)](https://apis.map.kakao.com/)
[![Google Gemini](https://img.shields.io/badge/Google_Gemini-8E75B7?style=for-the-badge&logo=google-gemini&logoColor=white)](https://deepmind.google/technologies/gemini/)

#### **개발 도구 (Development Tools)**
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)](https://www.jetbrains.com/idea/)
[![DBeaver](https://img.shields.io/badge/DBeaver-382923?style=for-the-badge&logo=dbeaver&logoColor=white)](https://dbeaver.io/)

#### **협업/커뮤니케이션 (Collaboration/Communication)**
[![KakaoTalk](https://img.shields.io/badge/KakaoTalk-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)](https://www.kakaocorp.com/service/KakaoTalk)

### **👨‍💻 팀원 소개 및 역할**
| 팀원 (Team Member) | 역할 (Role) | 주요 담당 기능 (Responsibilities) |
| --- | --- | --- |
| [**유승주**](https://github.com/pheonixpark) | PM, AI/ML Lead | - CNN/YOLOv8 기반 암 예측 모델 개발<br>- 프로젝트 총괄 및 관리 |
| [**민경준**](https://github.com/minkj98) | PL, AI Chatbot Lead | - Gemini API 연동 챗봇 기능 구현<br>- 프로젝트 개발 총괄 |
| [**박지선**](https://github.com/dodidosid) | Backend Developer | - Spring Security 기반 로그인/회원가입 구현<br>- 식단 추천 기능 구현 |
| [**이민우**](https://github.com/LeeMinWoo98) | Backend Developer | - Kakao Map API 연동 주변 병원 추천 구현<br>- 식단 추천 기능 구현 |

