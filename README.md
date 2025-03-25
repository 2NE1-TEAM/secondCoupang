
<br>

# 👀 Welcome
### [ 대규모 AI 시스템 프로젝트 ]
###  물류 관리 및 배송 시스템을 위한 MSA 기반 플랫폼 개발


<br>


## 👨‍👩‍👧‍👦 Our Team

|전인종|염금성|김정환|양현진|
|:---:|:---:|:---:|:---:|
|[@jnjongjeon](https://github.com/jnjongjeon)|[@venus-y](https://github.com/venus-y)|[@JeongHwan95](https://github.com/JeongHwan95)|[@woolala](https://github.com/woo-lala)|
|BE|BE|BE|BE|


<br><br>



## 📝 Technologies & Tools (BE) 📝

<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/> <img src="https://img.shields.io/badge/SpringSecurity-6DB33F?style=for-the-badge&logo=SpringSecurity&logoColor=white"/> <img src="https://img.shields.io/badge/JSONWebToken-000000?style=for-the-badge&logo=JSONWebTokens&logoColor=white"/>

<div>    
  <img src="https://hackmd.io/_uploads/BJ4JLo16yl.png" width="100px" height="60px" style="border-radius: 10px; border: 2px solid #ddd; padding: 5px;"/>
  <img src="https://hackmd.io/_uploads/HkKtIjypkx.png" width="100px" height="60px" style="border-radius: 10px; border: 2px solid #ddd; padding: 5px;"/>
  <img src="https://hackmd.io/_uploads/B1CiLjJaJg.png" width="100px" height="60px" style="border-radius: 10px; border: 2px solid #ddd; padding: 5px;"/>
</div>
<div>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"/> 
    <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"/>  
    <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white"/>
    <img src="https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka">
    <img src="https://img.shields.io/badge/Tracing-Zipkin-brightgreen?logo=apache" alt="Zipkin" height="25">
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
</div>
<div>
    <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"/>
    <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"/> 
    <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"/> 
    <img src="https://img.shields.io/badge/IntelliJIDEA-000000?style=for-the-badge&logo=IntelliJIDEA&logoColor=white"/>
    <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white"/> 
    <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white"/> 
</div>
<div>
    <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"/> 
    <img src="https://img.shields.io/badge/google%20gemini-8E75B2?style=for-the-badge&logo=google%20gemini&logoColor=white"/>
</div>
<br><br>





## 프로젝트 기능

### 🛡  JWT 토큰 인증 로그인

> * Refresh Token과 Access Token은 로그인을 통해 발급됩니다.
> * Refresh token은 서버에 저장됩니다.
> * Access Token은 로그아웃 시 redis에 black list token으로 저장됩니다. 그 후 로그아웃 된 토큰으로 로그인 시 로그아웃이 거부됩니다.

<br>

### 🛡  GEMINI API와 Slack API 연동

> * AI에게 배송지, 경유지, 목적지를 주면 해당 정보들을 기반으로 발송 담당자가 물건을 발송해야 할 최소 발송 시한을 받습니다. 또한 최소 발송시한은 슬랙 채널방에 올라갑니다.
> * 주문 결제가 완료, 허브 생성 실패 시 슬랙 채널로 해당 내용이 전달됩니다.

<br>

### 🛡  주문 동기/비동기 구현

> * 주문 요청이 들어오면, 업체와 재고 검증을 동기적으로 실행합니다.
> * 주문이 성공적으로 처리되면, 결제 메시지를 발행해 비동기적으로 결제 처리를 진행합니다.
> * 결제가 성공적으로 처리되면 슬랙에 메시지가 전송되고, 배송 메시지를 발행하고 배송 처리가 시작됩니다.
> * 결제가 실패하면 재고 복원 요청을 진행하고 주문이 취소됩니다.
> * 배송이 실패하면 재고 복원 요청과 결제 취소 메시지가 발행된 후 주문이 취소됩니다.



 <br>


### 🛡  허브간 최단 이동경로 구현

> * 허브 간 거리가 200km 미만일 경우: P2P(직접 배송) 방식으로 처리
> * 허브 간 거리가 200km 이상일 경우: 다익스트라 알고리즘을 이용해 중간 허브를 경유한 최적 경로를 계산
> * 허브 간 거리 정보를 기반으로 그래프 구성 모든 허브 간 거리를 HubDistance 테이블에 저장해두고, 이를 기반으로 그래프를 생성한다. ex)17개 허브면 17*17개의 데이터 저장
> * 다익스트라 알고리즘을 이용한 최단 경로 탐색 최단 경로가 필요한 경우, 시작 허브와 도착 허브를 기준으로 그래프를 탐색하여 최단 경로를 반환.
> * Redis를 활용한 경로 캐싱 허브 생성은 자주 발생하지 않으므로, 반복적인 경로 요청에 대해 Redis를 활용한 결과 캐싱을 통해 성능을 극대화.






<br><br>




## 적용 기술

### ◻ QueryDSL

> 커서페이징, 정렬, 검색어 등에 따른 동적 쿼리 작성을 위하여 QueryDSL 도입하여 활용했습니다.

### ◻ Redis

> 연속된 요청으로 인한 DB병목을 해소하고 RefreshToken, AccessToken 등 소멸기간이 존재하는 데이터의 TimeToLive 관리를 용이하게 할 수 있도록 Redis를 도입하였습니다.

### ◻ Kafka

> MSA 프로젝트로 개발이므로 허브 생성 후 허브 거리 생성, 주문 프로세스, 배송 프레스를 Kafka를 이용해서 비동기 처리하였습니다.

<br><br>

## 🚨 Trouble Shooting

#### Kafka 롤백과 스프링 트랜잭션의 관계 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/kafka-%EB%A1%A4%EB%B0%B1%EA%B3%BC-%EC%8A%A4%ED%94%84%EB%A7%81-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%EC%9D%98-%EA%B4%80%EA%B3%84)
#### 레디스 블랙리스트 access token 처리 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%5BTrouble-Shooting%5D-Redis%EC%9D%98-%EB%B8%94%EB%9E%99%EB%A6%AC%EC%8A%A4%ED%8A%B8-Access-Token,-Refresh-Token-%EC%B2%98%EB%A6%AC#%EB%AC%B8%EC%A0%9C)
#### Kafka 사용에 따른 스프링 이벤트 발행  [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/Kafka-%EC%82%AC%EC%9A%A9%EC%97%90-%EB%94%B0%EB%A5%B8-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EB%B0%9C%ED%96%89-%EC%82%AC%EC%9A%A9)
#### 쓰레드로컬과 인터셉터를 통한 인증정보 관리 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EC%93%B0%EB%A0%88%EB%93%9C-%EB%A1%9C%EC%BB%AC%EA%B3%BC-%EC%9D%B8%ED%84%B0%EC%85%89%ED%84%B0%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%9C-%EC%9D%B8%EC%A6%9D%EC%A0%95%EB%B3%B4-%EA%B4%80%EB%A6%AC)

<br><br>

## :raising_hand::thought_balloon: Concern
####  다익스트라 알고리즘으로 허브간 배송 경로 최적화 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EB%8B%A4%EC%9D%B5%EC%8A%A4%ED%8A%B8%EB%9D%BC-%EC%95%8C%EA%B3%A0%EB%A6%AC%EC%A6%98%EC%9C%BC%EB%A1%9C-%ED%97%88%EB%B8%8C%EA%B0%84-%EB%B0%B0%EC%86%A1-%EA%B2%BD%EB%A1%9C-%EC%B5%9C%EC%A0%81%ED%99%94)
####  Redis cachemanager 사용 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/Redis-cacheManager-%EC%82%AC%EC%9A%A9)
####  카카오 API를 이용해서 실시간 거리 계산 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EC%B9%B4%EC%B9%B4%EC%98%A4-API%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%B4%EC%84%9C-%EC%8B%A4%EC%8B%9C%EA%B0%84-%EA%B1%B0%EB%A6%AC-%EA%B3%84%EC%82%B0)
####  카카오 API를 이용해서 실시간 거리 계산 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EC%B9%B4%EC%B9%B4%EC%98%A4-API%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%B4%EC%84%9C-%EC%8B%A4%EC%8B%9C%EA%B0%84-%EA%B1%B0%EB%A6%AC-%EA%B3%84%EC%82%B0)
####  커서페이징 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EC%BB%A4%EC%84%9C-%ED%8E%98%EC%9D%B4%EC%A7%95-%EA%B5%AC%ED%98%84)
####  ErrorDecoder 사용 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/FeignClient-ErrorDecoder---@RestControllerAdvice-%EC%82%AC%EC%9A%A9)
####  전화번호 통일 유틸 사용 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EC%A0%84%ED%99%94%EB%B2%88%ED%98%B8-%ED%86%B5%EC%9D%BC-%EC%9C%A0%ED%8B%B8-%EC%82%AC%EC%9A%A9)
####  주문 동기/비동기 프로세스 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/%EC%A3%BC%EB%AC%B8-%EB%8F%99%EA%B8%B0-%EB%B9%84%EB%8F%99%EA%B8%B0-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4)
####  Redis cachemanager 사용 [WIKI보기](https://github.com/2NE1-TEAM/secondCoupang/wiki/Redis-cacheManager-%EC%82%AC%EC%9A%A9)

<br><br>

## 🌐 Architecture

![image](https://hackmd.io/_uploads/rkDPGs1aJl.png)


<br>

## [📋 ERD Diagram](https://www.erdcloud.com/d/BXLhRvZFfGiv2ttqe)
![2ne1-쿠팡 (1)](https://hackmd.io/_uploads/SyL-7oka1g.png)



<br>


