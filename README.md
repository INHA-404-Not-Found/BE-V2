<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=venom&color=0:004C97,100:007AC1&height=270&text=Lost%20INHA&fontSize=46&fontColor=D1D5DB&animation=fadeIn" />
</div>


## 기술 스택
- Frontend: JavaScript, HTML5, CSS, React, React Native, Expo, Redux
- Backend: Spring Boot, Spring Data JPA, Spring Security, JWT, MySQL

## 사용 툴
- Tools: Git Hub, Notion, Postman, Figma

## 배포
- Infra: Amazon EC2, RDS, NGINX

<br><br><br>


## LOST-INHA

인하대학교 분실물 통합 관리 플랫폼입니다.
교내 분실물을 최소화하고 학생들의 학교 생활의 질을 향상시켜주고자 합니다.

<br>

## 프로젝트 소개

LOST INHA는 캠퍼스 내 분실물/습득물을 빠르고 체계적으로 관리할 수 있는 통합 플랫폼입니다. 기존의 아날로그 중심 분실물 관리 방식을 개선하여, 학생과 관리자 모두가 효율적으로 정보를 공유하고 확인할 수 있도록 설계되었습니다.

- 관리자 (웹): 분실물/습득물 조회 및 일괄 수정, 수령자 정보 등록, 물품 카테고리 등록 및 수정 가능
- 학생 (앱): 분실물/습득물 등록, 카테고리별/분실 위치별/상태별 검색 가능

이 플랫폼을 통해 관리자는 분실물 관리 업무의 효율성을 높이고, 학생은 언제 어디서나 간편하게 물품을 등록하고 조회할 수 있는 편리한 사용자 경험을 제공합니다.

<br>

<br>

## 주요기능

- JWT 기반 인증 로그인
- 편리한 분실/습득 게시물 등록
- 분실 신고 해두면 사용자 맞춤 알림 전송
- 게시판의 편리한 분실물 필터링 

<br>

## 시스템 아키텍처
<img width="1109" height="474" alt="Image" src="https://github.com/user-attachments/assets/05add923-f9ce-485f-a4ed-e4b9ced12d89" />


<br>



## 프로젝트 구조

### Frontend
  #### - 웹
    +---public
    |   +---fonts
    |   \---images
    \---src
      +---api
      +---assets
      +---components
      +---fonts
      +---pages
      |   +---itemCategory
      |   +---login
      |   +---main
      |   +---post
      |   \---receiverRegist
      +---styles
      \---utils

  #### - 앱
  
    MOBILE
    ├── .expo
    │   └── web
    ├── .vscode
    ├── android
    ├── api
    │   ├── api.js
    │   ├── auth.js
    │   ├── category.js
    │   ├── location.js
    │   ├── post.js
    │   └── receiver.js
    ├── assets
    ├── components
    │   ├── BottomBar.js
    │   ├── CategoryList.js
    │   ├── DefaultHeader.js
    │   ├── LocationMap.js
    │   ├── LocationViewBox.js
    │   ├── MyPostListItem.js
    │   ├── Notification.js
    │   ├── PostListItem.js
    │   ├── PostTypeSelector.js
    │   ├── SearchHeader.js
    │   ├── SelectCate.js
    │   └── StatusLabel.js
    ├── hooks
    │   └── useAuth.js
    ├── node_modules
    ├── Redux
    │   ├── slices
    │   │   ├── categorySlice.js
    │   │   ├── keywordSlice.js
    │   │   ├── locationSlice.js
    │   │   └── mySlice.js
    │   └── store.js
    ├── screens
    │   ├── AddLostPostScreen.js
    │   ├── AddPostScreen.js
    │   ├── EditPostScreen.js
    │   ├── Login.js
    │   ├── MainScreen.js
    │   ├── MyPostListScreen.js
    │   ├── NotificationListScreen.js
    │   ├── PostListScreen.js
    │   ├── PostScreen.js
    │   └── UserScreen.js
    ├── utils
    │   ├── DateFormat.js
    │   └── imageSource.ts
    ├── .gitignore
    ├── App.js
    ├── app.json
    ├── babel.config.js
    ├── package-lock.json
    ├── package.json
    ├── setupProxy.js
    ├── tokenStorage.js
    └──  TokenStore.js

### Backend

    +---domain
    |   +---category
    |   |   +---api
    |   |   +---dto
    |   |   |   +---request
    |   |   |   \---response
    |   |   +---model
    |   |   +---repository
    |   |   \---service
    |   +---comment
    |   |   +---api
    |   |   +---dto
    |   |   |   +---request
    |   |   |   \---response
    |   |   +---model
    |   |   +---repository
    |   |   \---service
    |   +---location
    |   |   +---api
    |   |   +---dto
    |   |   |   +---request
    |   |   |   \---response
    |   |   +---model
    |   |   +---repository
    |   |   \---service
    |   +---member
    |   |   +---model
    |   |   \---repository
    |   +---notification
    |   |   +---api
    |   |   +---dto
    |   |   |   +---request
    |   |   |   \---response
    |   |   +---model
    |   |   +---repository
    |   |   \---service
    |   +---post
    |   |   +---api
    |   |   +---dto
    |   |   |   +---request
    |   |   |   \---response
    |   |   +---model
    |   |   +---repository
    |   |   \---service
    |   \---receiver
    |       +---api
    |       +---dto
    |       |   +---request
    |       |   \---response
    |       +---model
    |       +---repository
    |       \---service
    \---global
        +---auth
        |   +---token
        |   |   +---api
        |   |   +---dto
        |   |   |   +---request
        |   |   |   \---response
        |   |   +---exception
        |   |   +---filter
        |   |   \---service
        |   \---user
        +---config
        |   +---auth
        |   +---firebase
        |   \---web
        +---firebase
        |   +---api
        |   +---dto
        |   |   \---request
        |   +---model
        |   +---repository
        |   \---service
        \---mail
        +---api
        \---service
<br>

## 개발환경

### Version

- Spring boot: 3.x

- Java: JDK 21

- Mysql: 8.xx

- React: 19.x.x

- React Native: 0.81.x

- expo: 54.x.x

### 환경 설정 및 실행
#### FE
  - 웹
    
    1️. 프로젝트 클론
    
    git clone https://github.com/INHA-404-Not-Found/ADMIN.git
  
    2️. 의존성 설치
    
    npm install
  
    3️. expo 실행
    
    npm start
  
  - 앱
    
    1️. 프로젝트 클론

    git clone https://github.com/INHA-404-Not-Found/FE.git
  
    2️. 의존성 설치
    
    npm install
  
    3️. expo 실행
    
    npm start



#### BE
  1️. 프로젝트 클론
  
  git clone https://github.com/INHA-404-Not-Found/BE.git

  2️. 의존성 설치
  
  ./gradlew build

  3️. 환경 변수 설정
  
      -- resources/application.properties DB, 환경변수 등 설정 --
      spring.application.name=next_campus
  
      # DB 관련
      spring.datasource.url=${YOUR_DB_URL}
      spring.datasource.username=${YOUR_DB_USERNAME}
      spring.datasource.password=${YOUR_DB_PASSWORD}
      spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
  
      # JWT 인증 관련
      jwt.secret=${YOUR_JWT_SECRET}
      jwt.access-token-expiration-ms=${YOUR_JWT_ACCESS_EXPIRATION}
      jwt.refresh-token-expiration-ms=${YOUR_JWT_REFRESH_EXPIRATION}
  
      # 이메일 알림 관련
      spring.mail.host=smtp.gmail.com
      spring.mail.port=587
      spring.mail.username=${YOUR_MAIL_USERNAME}
      spring.mail.password=${YOUR_MAIL_PASSWORD}
      
      spring.mail.properties.mail.smtp.auth=true
      spring.mail.properties.mail.smtp.starttls.enable=true
  
      # DB 테이블 정보 가져오기
      spring.jpa.hibernate.ddl-auto=update
    
      server.port=8080
      server.ssl.enabled=false
  4️. 실행
  <br>
  ./gradlew bootRun

  5️. 테스트
  <br>
  Postman으로 API를 테스트 
  <img width="1000" height="500" alt="KakaoTalk_20251107_151856585" src="https://github.com/user-attachments/assets/64da4290-b43c-4426-8984-4833774f5c90" />
  <img width="1000" height="500" alt="KakaoTalk_20251107_151856585_01" src="https://github.com/user-attachments/assets/2fa07143-f253-46fa-a2ea-f69ed8b61069" />
  <img width="1000" height="500" alt="KakaoTalk_20251107_151856585_02" src="https://github.com/user-attachments/assets/6aba0704-8b11-43af-806b-9e24b34b8049" />
  <img width="1000" height="50" alt="KakaoTalk_20251107_151856585_03" src="https://github.com/user-attachments/assets/409460ce-25e7-4af4-8ae8-56628978ab31" />

  


<br>

## ERD
<img width="2500" height="1422" alt="inha_next_campus_db (3)" src="https://github.com/user-attachments/assets/027d5b5e-168d-42b2-b565-c387e3c96b22" />


<br>

## <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Activities/Sparkles.png" alt="Sparkles" width="25" height="25" /> 팀원 소개

|                                  Frontend                                   |                                  Frontend                                   |                                    Backend                                    |                                   Backend                                   |
|:---------------------------------------------------------------------------:|:---------------------------------------------------------------------------:|:-----------------------------------------------------------------------------:|:---------------------------------------------------------------------------:|
| <img src="https://avatars.githubusercontent.com/u/123297062?v=4" width=100> | <img src="https://avatars.githubusercontent.com/u/165632710?v=4" width=100> | <img src="https://avatars.githubusercontent.com/u/155566596?v=4" width=100>  | <img src="https://avatars.githubusercontent.com/u/181314146?v=4/u/000000000?v=4" width=100> |
|                                     김도담                                     |                                     안유민                                     |                                      권도연                                      |                                   최지윤(팀장)                                   |

<br>
