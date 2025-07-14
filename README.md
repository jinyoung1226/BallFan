# ⚾️ 야구 직관 기록 앱 BallFan  ⚾️

## 프로젝트 소개

🏷 **프로젝트 명 : BallFan**

🗓️ **프로젝트 기간 : 2025.02.01 ~ 2025.06.23**

👥 **구성원 : 김효민(PM, Design, FE), 김수환(FE), 김동현(FE), 임진영(BE, Infra, AI), 심용석(AI)**

🏆 **수상 : 아주대학교 SOFTCON 개발 부문 우수상**

---

### 😎 서비스 구경 바로가기

**🎥 서비스 소개 영상 : https://softcon.ajou.ac.kr/works/works.asp?uid=2085**

---

### ✅ 기획 배경 및 서비스 소개
![BallFan 기획 배경 및 서비스 소개1.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C1.png)
![BallFan 기획 배경 및 서비스 소개2.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C2.png)
![BallFan 기획 배경 및 서비스 소개3.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C3.png)
![BallFan 기획 배경 및 서비스 소개4.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C4.png)
![BallFan 기획 배경 및 서비스 소개5.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C5.png)
![BallFan 기획 배경 및 서비스 소개6.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C6.png)
![BallFan 기획 배경 및 서비스 소개7.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C7.png)
![BallFan 기획 배경 및 서비스 소개8.png](Readme_assets/BallFan%20%EA%B8%B0%ED%9A%8D%20%EB%B0%B0%EA%B2%BD%20%EB%B0%8F%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%86%8C%EA%B0%9C8.png)

---


## 💌 서비스 화면 및 기능 소개

### ✅ 서비스 흐름도

- **회원가입 및 마이팀 설정**
> 이메일, 비밀번호, 닉네임, 마이팀을 입력 받아 회원가입을 진행한다.

<p align="center">
  <img src="Readme_assets/회원가입.gif" alt="회원가입 기능 시연" width="270px" />
</p>

---

- **로그인 및 홈 화면(티켓 저장소)**
> 회원가입 시, 가입했던 정보를 바탕으로 로그인을 진행한다.

<p align="center">
  <img src="Readme_assets/로그인 및 홈화면.gif" alt="로그인 기능 시연" width="270px" />
</p>

---

- **OCR 기반 티켓 등록 및 확인**
> PaddleOCR(GPU 기반)을 활용해 티켓 사진에서 경기 날짜와 원정팀 정보를 추출한다.  
> 추출한 정보를 기반으로, 스탯티즈 야구 기록 사이트에서 자동 크롤링한 경기 데이터와 매핑하여 티켓을 저장한다.  
> 사용자는 홈 화면에서 등록된 티켓을 통해 다양한 경기 정보를 확인할 수 있다.

<p align="center">
  <img src="Readme_assets/티켓 등록 및 확인.gif" alt="OCR 티켓 등록 시연" width="270px" />
</p>

---

- **티켓 리뷰 등록 및 확인**
> 1. 사용자가 입력한 티켓 리뷰 텍스트를 OpenAI 임베딩 모델로 벡터화하여 Chroma DB에 저장한다.
> 2. 리뷰와 함께 입력된 좌석 정보도 함께 저장되어 이후 추천 기준으로 활용한다.
> 3. 리뷰 텍스트를 LangChain을 통해 분석하고, OpenAI와 Naver Search API를 이용해 리뷰에서 장소 관련 키워드를 추출한다.
> 4. 추출된 장소 키워드를 기반으로 Naver Search API에서 해당 장소의 위경도 정보를 받아온다.
> 5. 위경도 정보를 프론트로 전달하여 Naver Map API를 통해 지도에 해당 장소를 시각화한다.
> 6. 저장된 리뷰 벡터와 좌석 정보를 기반으로, 유사한 좌석 리뷰나 내용이 비슷한 리뷰를 사용자에게 추천한다.
> 7. 리뷰 텍스트를 기반으로 LLM이 분석하여 리뷰에 어울리는 키워드 역시 추천한다.
> 8. 이를 통해 사용자들은 유사 좌석에 대한 시야나 분위기 등의 정보를 간접적으로 파악할 수 있다.

<p align="center">
  <img src="Readme_assets/티켓 리뷰 등록.gif" alt="티켓 리뷰 등록" width="270px" />
  <img src="Readme_assets/티켓 리뷰 확인.gif" alt="티켓 리뷰 확인" width="270px" />
</p>
<p align="center">
  <img src="Readme_assets/리뷰 확인.gif" alt="리뷰 분석 결과 확인" width="270px" />
</p>

---

- **명예의 전당(승률 및 방문 경기장)**
> 사용자가 설정한 응원 팀 기준으로, 팀 내 유저들의 직관 승률을 비교하여 최고 승요를 확인할 수 있다.  
> 월간 기준으로 가장 높은 승률을 기록한 유저를 ‘이달의 승요’로 표시한다.  
> 전체 사용자 중 승률이 가장 높은 유저를 조회할 수 있는 기능을 제공한다.  
> 사용자가 방문한 모든 경기장 목록과 각 경기장별 방문 횟수를 확인할 수 있다.  
> 경기장을 방문할 때마다 스탬프가 지급되어 사용자에게 기록과 수집의 재미를 제공한다.

<p align="center">
  <img src="Readme_assets/명예의 전당 승률 확인.gif" alt="승률 확인" width="270px" />
  <img src="Readme_assets/명예의 전당 방문경기장.gif" alt="방문 경기장 확인" width="270px" />
</p>

---

- **마이 홈(내 리뷰 확인 및 프로필 수정)**
> 마이홈에서 자신이 작성한 리뷰를 모아서 확인할 수 있다.  
> 자신의 닉네임 및 프로필 사진을 변경할 수 있다.

<p align="center">
  <img src="Readme_assets/마이홈 리뷰 확인.gif" alt="리뷰 확인" width="270px" />
  <img src="Readme_assets/마이홈 정보 수정.gif" alt="프로필 수정" width="270px" />
</p>

> 마이홈에서 자신이 좋아요를 누른 리뷰를 확인할 수 있다.  
> 앱을 종료하고 싶다면 로그아웃을 진행한다.

<p align="center">
  <img src="Readme_assets/마이홈 스크랩.gif" alt="스크랩 리뷰" width="270px" />
  <img src="Readme_assets/로그아웃.gif" alt="로그아웃" width="270px" />
</p>


## 🛠 기술 스택

### FE
<p>
  <img src="https://img.shields.io/badge/ReactNative-61DAFB?style=flat-square&logo=React&logoColor=white">
  <img src="https://img.shields.io/badge/Zustand-3578E5?style=flat-square&logoColor=white">
  <img src="https://img.shields.io/badge/Axios-CA4241?style=flat-square&logo=axios&logoColor=white">
  <img src="https://img.shields.io/badge/Typescript-06B6D4?style=flat-square&logo=TypeScript&logoColor=black"/>
  <img src="https://img.shields.io/badge/Naver Map API-03C75A?style=flat-square&logo=naver&logoColor=white"/>
</p>


### BE
<p>
	<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
	<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
	<img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=OpenJDK&logoColor=white"/>
</p> 

### AI
<p>
	<img src="https://img.shields.io/badge/PaddlePaddle-0062B0?style=flat-square&logo=paddlepaddle&logoColor=white"/>
	<img src="https://img.shields.io/badge/GPU(RTX 3060)-76B900?style=flat-square&logo=nvidia&logoColor=white"/>
	<img src="https://img.shields.io/badge/LangChain-1C3C3C?style=flat-square&logo=langchain&logoColor=white"/>
    <img src="https://img.shields.io/badge/OpenAI-412991?style=flat-square&logo=openai&logoColor=white"/>
    <img src="https://img.shields.io/badge/Chroma DB-512BD4?style=flat-square&logo=&logoColor=white"/>
    <img src="https://img.shields.io/badge/Naver Search API-03C75A?style=flat-square&logo=naver&logoColor=white"/>
    <img src="https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=Fastapi&logoColor=white"/>
</p> 

### DB
<p>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=black"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
</p>


### Dev-Ops
<p>
    <img src="https://img.shields.io/badge/Amazon S3-569A31?style=flat-square&logo=amazonS3&logoColor=white"/>
	<img src="https://img.shields.io/badge/jenkins-D24939?style=flat-square&logo=jenkins&logoColor=white"/>
	<img src="https://img.shields.io/badge/docker-2496ED?style=flat-square&logo=docker&logoColor=white"/>
	<img src="https://img.shields.io/badge/nginx-009639?style=flat-square&logo=nginx&logoColor=white"/>
    <img src="https://img.shields.io/badge/ubuntu-E95420?style=flat-square&logo=ubuntu&logoColor=white"/>
</p>


### Communication
<p>
	<img src="https://img.shields.io/badge/figma-F24E1E?style=flat-square&logo=figma&logoColor=white">
	<img src="https://img.shields.io/badge/notion-000000?style=flat-square&logo=notion&logoColor=white">
	<img src="https://img.shields.io/badge/Github-181717?style=flat-square&logo=github&logoColor=white">
    <img src="https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white">
</p>



## 🗂 프로젝트 구조

### FE
#### https://github.com/hwansoo17/ballfan

```markdown
└─📦 src
  ├─📂 api
  ├─📂 assets
  │  ├─📂 fields
  │  ├─📂 fonts
  │  ├─📂 svgs
  ├─📂 bottomTabs
  ├─📂 components
  ├─📂 bottomTabs
  ├─📂 hooks
  │  ├─📂 mutation
  ├─📂 nav
  ├─📂 screens
  │  ├─📂 auth
  │  ├─📂 home
  │  ├─📂 mypage
  │  ├─📂 ranking
  │  ├─📂 record
  ├─📂 store
  ├─📂 styles 
  └─📜 App.tsx
```

---

### BE
#### https://github.com/jinyoung1226/BallFan

```markdown
├─📂 java
    📦 BallFan
    ├─📂 authentication
    │  ├─📂 filter
    ├─📂 controller
    ├─📂 dto
    │  ├─📂 auth
    │  ├─📂 line_up
    │  ├─📂 pitcher
    │  ├─📂 record
    │  ├─📂 response
    │  ├─📂 review
    │  ├─📂 stadium
    │  ├─📂 ticket
    │  └─📂 user
    ├─📂 entity
    │  ├─📂 pitcher
    │  ├─📂 review
    │  ├─📂 token
    │  └─📂 user
    ├─📂 exception
    │  ├─📂 auth
    │  ├─📂 review
    │  ├─📂 stadium
    │  ├─📂 ticket
    │  ├─📂 user
    ├─📂 jackson
    ├─📂 repository
    ├─📂 s3
    ├─📂 service
    ├─🧩 BallFanApplication
```

## 📜 프로젝트 산출물

### 시스템 아키텍쳐
![BallFan_Architecture.png](Readme_assets/BallFan_Architecture.png)
---

### ERD
![BallFan_ERD.png](Readme_assets/BallFan_ERD.png)

---

### API 명세서
![BallFan_API Specification.png](Readme_assets/BallFan_API%20Specification.png)

---

### 메뉴 트리
![BallFan_Menu Tree.png](Readme_assets/BallFan_Menu%20Tree.png)

---

### 정보구조도
![BallFan_IA.png](Readme_assets/BallFan_IA.png)

## 💙 팀원 소개
| 김효민 (PM, FE, Design)                                                                                              | 임진영 (BE, Infra, AI)                                                                                                                                     | 김수환 (FE)                                                    |
|------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| 기획 및 디자인 총괄 <br> Figma 기반 UI/UX 설계 <br> 기능 요구사항 정의 및 흐름도 작성 <br> 전당 페이지 구현 <br> 마이홈 페이지 구현 | 백엔드 및 인프라 총괄 <br> DB 모델링 및 API 개발 <br> CI/CD 파이프라인 구축 <br> Nginx 및 SSL 인증서 설정 <br> LangChain 기반 리뷰 분석 시스템 및 벡터 DB 연동 <br> 유사 리뷰 추천 기능 및 장소 키워드 추출 API 개발 | 회원가입 및 로그인 로직 구현 <br> JWT 토큰 인증 방식 구현 <br> 프론트엔드 디렉토리 구조 설계 |
| https://github.com/rlagyals                                                                                        | https://github.com/jinyoung1226                                                                                                                          | https://github.com/hwansoo17                                |

| 김동현(FE)                                                                                       | 심용석(AI)                                                                                                                        |
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| Front-End 총괄 <br> 회원가입 및 로그인 페이지 구현 <br> 홈, 티켓 등록 페이지 구현 <br> 기록 페이지 구현 <br> Naver Map API 연동 | AI 총괄 <br> PaddleOCR 기반 야구 지류 티켓 인식 모델 개발 <br> 경기 결과 자동 수집을 위한 크롤링 시스템 구축 <br> FastAPI 기반 크롤링 API 개발 및 DB 연동                   |
| https://github.com/DongDongsqq                                                                 | https://github.com/yongseokSim                                                                                   |