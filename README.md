![image](https://user-images.githubusercontent.com/58943830/190537888-a808062d-fe3a-41a1-bf2e-d407627c329f.png)
## 👉 UandMeet 소개
맟춤 운동 매칭 , 정보 공유 사이트 입니다.
<br>
[ # 너,나 만나 ](http:uandmeet.shop)
<br>
## 프로젝트 개요
지역별로 같이 운동할 사람을 매칭하고, 정보까지 공유할 수 있는
<br>
게시판 위주의 사이트입니다.

## 🛠 Architecture
![image](https://user-images.githubusercontent.com/56526225/190002286-d5fa17f1-a672-462f-b5da-fd9303cddee4.png)


#### 🗓 2022.08.05 - 2022.09.16 (6주)
#### 🙋‍♂️ 팀원

<table>
  <tr>
    <td colspan="2">Front-End</td>
    <td colspan="4">Back-End</td>
  </tr>
  <tr>
    <td>조현오</td>
    <td>박정원</td>
    <td>이민호</td>
    <td>이성훈</td>
    <td>장정훈</td>
    <td>홍산의</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=React&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=React&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Springboot-6DB33F?style=flat-square&logo=Springboot&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Springboot-6DB33F?style=flat-square&logo=Springboot&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Springboot-6DB33F?style=flat-square&logo=Springboot&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Springboot-6DB33F?style=flat-square&logo=Springboot&logoColor=white"/></td>
  </tr>
</table>


#### 🔗 link

- [시연영상]()
- [Team notion](https://www.notion.so/4-2cfecbfb7da547f58028d85890da61e8)
- [Github Back-End repo](https://github.com/enkidur/uandmeet)
- [Github Front-End repo](https://github.com/letsjo/exercise-match)
<br>

## 🚀 주요 작업 및 기능
#### 😃 소셜 로그인을 통한 안전하고 간편한 로그인과 회원가입 
- 직접적으로 사람이 만나는 사이트이므로 최소한의 보안을 위해 개인 정보를 요구. 하지만 귀찮은 회원가입이 해당 사이트에 대해 진입장벽을 만든다고 생각하여 소셜 로그인을 구현하여 간단하게 가입 가능하도록 구현
#### 😃 JWT를 Access Token, Refresh Token으로 나누고, Redis를 사용하여 보안성을 높이고 서버 소스를 줄임
- cookie는 탈취 및 변경이 쉬움
- session은 서버에 저장하는 방식을 사용하는데 이는 접속량이 많을 경우 서버에 부하를 증가시킴
- 가벼우면서 조회가 빠르고 만료일을 지정할 수 있는 Redis를 사용
#### 😃 Mail 인증을 통해 회원가입, 비밀번호 찾기를 구현
- javaMai API를 통해 이메일을 인증
- 연속 인증을 통해 다량의 메일 인증을 막기 위해 redis를 통해 난수와 횟수를 저장하고 만료시간을 통해  3회 초과일 경우 일정 기간동안 메일 전송을 막음 
#### 😃 내가 원하는 지역에서 다양한 사람들과
- 현재 위치에 존재하는 사람들의 정보와 게시물을 볼 수 있음
#### 😃 원하는 키워드를 검색 기능으로 간편하게
- 제목과 내용, 제목 , 내용으로 검색 기능이 세분화
#### 😃 매칭 상대방에 대해 후기 리뷰를 남기기
- 매칭했던 상대방에게 후기와 평점
#### 😃 정보 공유를 원할땐 공유 게시판 사용하기
- 매칭뿐만 아니라 정보 공유만 하고 싶다면 공유 게시판을 이용 가능
#### 💡 불편한 사항이나 건의 사항이 있다면 운영자에게 알림톡 사용하기
- 불편한 사항이나 건의 사항을 직접 알림톡을 건의 가능
#### 💡 MySQL Replication을 통한 성능 개선
- write 작업은 Master DB에서 하게 하고, read 작업은 slave DB에서 수행하도록 서버를 구분하여  DB에 부담을 줄임. 

<br>

## 📌 Tools
<div align=center>
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=Spring Boot&logoColor=white">
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=Spring Security&logoColor=white">
<img src="https://img.shields.io/badge/codedeploy-6DB33F?style=for-the-badge&logo=codedeploy&logoColor=white">
<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=Java&logoColor=white">
<img src="https://img.shields.io/badge/JSON Web Tokens-000000?style=for-the-badge&logo=JSON Web Tokens&logoColor=white">
<img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white">
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
<img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=IntelliJ IDEA&logoColor=white">
<img src="https://img.shields.io/badge/Sourcetree-0052CC?style=for-the-badge&logo=Sourcetree&logoColor=white">
<img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white">
<img src="https://img.shields.io/badge/Slack-4A154B7?style=for-the-badge&logo=Slack&logoColor=white">
<img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white">
<img src="https://img.shields.io/badge/AmazonEC2-FF9900?style=for-the-badge&logo=AmazonEC2&logoColor=white">
<img src="https://img.shields.io/badge/Amazon S3-569A31?style=for-the-badge&logo=Amazon S3&logoColor=white">
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">
<img src="https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=Ubuntu&logoColor=white">
<img src="https://img.shields.io/badge/socket.io-010101?style=for-the-badge&logo=socket.io&logoColor=white">
<img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=Git&logoColor=white">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
<img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=for-the-badge&logo=GitHub Actions&logoColor=white">
<img src="https://img.shields.io/badge/kakao login-FFCD00?style=for-the-badge&logo=kakao&logoColor=black">
<img src="https://img.shields.io/badge/google login-4285F4?style=for-the-badge&logo=google&logoColor=white">
</div>

## ⚙ 트러블 슈팅
- 공공 API 통신
공공데이터 포털 API에서 [시/도/군]>[구]>[동]순으로 받아와야하는 문제점 -> Back-end를 통하지않고 속도가 불안정한 타 서버에서 계속 받아서 사용
<br>👉 Back-end에서 지역명을 2주 1회 업데이트 주기로 받아온다. Back-end에서 공공데이터포털 API를 통해 필요한 지역명들을 모두 받아와서 DB에 저장 후에 
원하는 데이터로 가공하여 Front-end로 전송

- ID의 통일
소셜 로그인중 하나인 kakao는 ID를 email형식으로만 넘겨주기에 기존 회원가입과 통일하는 과정에서 혼란을 야기
<br>👉 타 사이트에서 많이 사용되는 kakaoLogin을 없애기 보단, 다른 소셜로그인과 폼로그인에서도 ID를 Email형식으로 통일

- JWT 보안
몇 세대 전에 만료되었던 Access Token을 탈취하여 Refresh Token을 재발급할 수 있는 문제가 발생
<br>👉 Redis에 발급된 최신 Access Token을 넣어 재발급 시 최신 토큰인지 체크를 통해 재발급 보안을 향상


## 📃 ERD
![uandmeet](https://user-images.githubusercontent.com/56526225/190538893-2bf1ac1a-209b-426d-b864-0b85c9661a2b.png)
