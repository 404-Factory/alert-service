cd "c:\Users\user\OneDrive\Desktop\404\notification-service"; Set-Content -Path README.md -Value @'
# Smart Factory Alert Notification Module

## 📌 작업 개요

`notification-service`는 반도체 공정 모니터링 시스템에서 이상 감지 결과를 사용자에게 알림으로 전달하는 백엔드 서비스입니다.

이 서비스는 사용자에게 실제 확인이 필요한 경고/긴급 알림만 전달하도록 설계되어 있습니다.

---

## 🧩 백엔드에서 실제로 제공하는 기능

### 1. Alert API

`notification-service` 백엔드는 다음 REST API를 제공합니다.

| 기능            | Method | Endpoint               | 설명                    |
| ------------- | ------ | ---------------------- | --------------------- |
| 알림 목록 조회      | GET    | `/api/v1/alerts`       | 상태, 심각도 조건으로 알림 목록 조회 |
| 알림 개수 조회      | GET    | `/api/v1/alerts/count` | 상태별 알림 개수 조회          |
| 알림 단건 조회      | GET    | `/api/v1/alerts/{id}`  | 특정 알림 상세 조회           |
| 전체 알림 상태 업데이트 | PATCH  | `/api/v1/alerts`       | 일괄 알림 상태 변경           |
| 단건 알림 상태 업데이트 | PATCH  | `/api/v1/alerts/{id}`  | 특정 알림 상태 변경           |


### 2. 실제 컨트롤러 경로

실제 컨트롤러는 다음과 같이 정의되어 있습니다.

```java
@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {
    @GetMapping
    public Page<AlertResponse> getAlerts(...) { ... }

    @GetMapping("/count")
    public ResponseEntity<CountResponse> getCount(...) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlert(...) { ... }

    @PatchMapping
    public ResponseEntity<Void> updateAlerts(...) { ... }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateAlert(...) { ... }
}


## 🔧 API 상세 설명

### 알림 목록 조회

`GET /api/v1/alerts`

- `status`: UNREAD / READ
- `severity`: WARNING / CRITICAL
- 페이징 및 정렬 지원

### 알림 개수 조회

`GET /api/v1/alerts/count`

- `status` 파라미터로 상태별 개수 조회

### 알림 단건 조회

`GET /api/v1/alerts/{id}`

- 단일 알림의 상세 정보를 조회합니다.

### 전체 알림 상태 업데이트

`PATCH /api/v1/alerts`

- 요청 바디 `AlertUpdateRequest`로 여러 알림을 일괄 업데이트합니다.

### 단건 알림 상태 업데이트

`PATCH /api/v1/alerts/{id}`

- 특정 알림의 상태를 변경합니다.



## 🌱 현재 상태

### 완료된 작업

* `AlertController` API 구현
* 알림 목록 조회 및 검색 처리
* 알림 개수 조회 구현
* 알림 단건 조회 구현
* 전체/단건 알림 상태 업데이트 구현
* 백엔드 설정 개선 (`application.yml`)

### 진행 예정 작업

* 이상 감지 파트와 알림 생성 연동
---

## 📌 브랜치

'dev' merge완료

---

## ✅ 커밋 예시

```bash
git add README.md
git commit -m "docs: update notification-service README to match actual backend API"
git push origin feature/alert-service
```
'@
