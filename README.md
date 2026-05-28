# Smart Factory Alert Notification Module

## 📌 작업 개요

본 작업은 반도체 공정 모니터링 시스템에서 **이상 감지 결과를 사용자 알림으로 연결하는 Alert 기능**을 구현하기 위한 작업입니다.

센서 데이터에서 이상이 감지되면 해당 이상 정보가 `ANOMALY_LOG`로 기록되고, 사용자에게 전달할 필요가 있는 이상만 `ALERT`로 생성되도록 설계하였습니다.
이를 통해 모든 이상 데이터를 단순히 노출하는 것이 아니라, 실제 사용자가 확인해야 하는 경고/긴급 알림만 알림 센터에서 확인할 수 있도록 구성하였습니다.

---

## 🧩 담당 작업 범위

### 1. 알림 목록 페이지 구현

알림 센터 화면에서 사용자가 발생한 알림을 확인할 수 있도록 알림 목록 페이지를 구성하였습니다.

주요 기능은 다음과 같습니다.

* 알림 목록 조회
* 긴급/경고 알림 구분
* 알림 읽음 여부 표시
* 단건 알림 읽음 처리
* 전체 알림 읽음 처리
* 알림 발생 횟수 표시
* 최근 감지 시간 표시

작업 파일 예시:

```text
src/pages/NotificationListPage.tsx
```

---

### 2. 알림 데이터 타입 정의

프론트엔드에서 알림 데이터를 일관되게 다루기 위해 알림 관련 타입을 정의하였습니다.

```ts
export type AlertCategory = "ALL" | "WARNING" | "CRITICAL"
export type AlertSeverity = "WARNING" | "CRITICAL"

export type AlertItem = {
  alertId: number
  logId: number
  title: string
  message: string
  isRead: boolean
  createdTime: string
  severity: AlertSeverity
  occurrenceCount: number
  lastDetectedAt: string
}

export type AlertPageResponse = {
  content: AlertItem[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
```

---

### 3. 알림 API 연동

알림 센터에서 필요한 API를 프론트와 연결하였습니다.

| 기능          | Method | Endpoint                     | 설명             |
| ----------- | -----: | ---------------------------- | -------------- |
| 알림 목록 조회    |    GET | `/api/alerts`                | 알림 센터 목록 조회    |
| 미확인 알림 수 조회 |    GET | `/api/alerts/unread-count`   | 긴급/경고 알림 개수 조회 |
| 단건 읽음 처리    |  PATCH | `/api/alerts/{alertId}/read` | 특정 알림 읽음 처리    |
| 전체 읽음 처리    |  PATCH | `/api/alerts/read-all`       | 모든 알림 읽음 처리    |
| 알림 상세 조회    |    GET | `/api/alerts/{alertId}`      | 특정 알림 상세 조회    |

---

## 🔔 이상 감지와 알림 연결 구조

이상 감지 파트와 알림 파트는 다음 흐름으로 연결되도록 설계하였습니다.

```text
센서 데이터 수집
→ 이상감지 Rule 적용
→ 이상 발생 시 ANOMALY_LOG 저장
→ 이상감지 파트에서 알림 파트로 알림 생성 요청
→ 알림 파트에서 ALERT 생성 또는 기존 ALERT 업데이트
→ 프론트 알림 센터에서 사용자에게 표시
```

알림 파트는 이상감지 데이터를 직접 조회하는 방식이 아니라,
이상감지 파트에서 이상 발생 시 알림 생성 요청을 보내면 해당 요청을 받아 `ALERT`를 생성하는 구조입니다.

---

## 📮 이상감지 → 알림 생성 요청 구조

이상감지 파트에서 알림 파트로 전달할 요청 예시는 다음과 같습니다.

```http
POST /api/alerts/from-anomaly
```

```json
{
  "anomalyLogId": 1,
  "equipmentId": "EQ-01",
  "sensorType": "temperature",
  "ruleType": "RULE_1",
  "severity": "CRITICAL",
  "message": "EQ-01 설비의 온도 센서에서 기준 범위 초과 이상이 감지되었습니다.",
  "detectedAt": "2026-05-28T14:30:00"
}
```

알림 파트에서는 해당 요청을 바탕으로 다음 처리를 수행합니다.

1. 알림 생성 가능 여부 판단
2. 기존 동일 알림 존재 여부 확인
3. 신규 알림 생성 또는 기존 알림 업데이트
4. `occurrenceCount` 증가
5. `lastDetectedAt` 갱신
6. 미확인 알림 수 반영

---

## 🌱 현재 진행 상태

### 완료한 작업

* 알림 목록 페이지 구성
* 알림 데이터 타입 정의
* 알림 목록 조회 API 연동
* 미확인 알림 수 조회 API 연동
* 단건 알림 읽음 처리 기능 연결
* 전체 알림 읽음 처리 기능 연결
* 이상감지와 알림 연결 흐름 정리

### 진행 예정 작업

* 이상감지 파트에서 알림 파트로 알림 생성 요청 API 연결
* `POST /api/alerts/from-anomaly` 구현
* 동일 알림 중복 처리 로직 고도화
* Redis cooldown key 기반 스팸 알림 방지 적용
* 실제 이상감지 데이터와 Alert 생성 로직 통합 테스트

---

## 🛠️ Git 작업 브랜치

```text
feature/alert-page
```

---

## ✅ Commit 예시

```bash
git add README.md
git commit -m "docs: add alert feature readme"
git push origin feature/alert-page
```
