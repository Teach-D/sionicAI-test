# API 명세 — 사용자 피드백(Feedback) 관리

- **브랜치**: `feat/feedback`
- **작성일**: 2026-07-14
- **Base URL**: `/api`

---

## 1. 공통

### 인증 방식

모든 엔드포인트는 JWT 인증이 필요하다.

```
Authorization: Bearer <accessToken>
```

### 공통 에러 응답 형식

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "오류 메시지",
  "timestamp": "2026-07-14T10:00:00+09:00"
}
```

### 공통 에러 코드

| HTTP Status | code | 발생 상황 |
|---|---|---|
| `400` | `VALIDATION_ERROR` | 요청 바디 유효성 검사 실패 |
| `401` | `UNAUTHORIZED` | 토큰 없음·만료·유효하지 않음 |
| `403` | `FORBIDDEN` | 타인 소유 대화에 피드백 생성 시도, 또는 ADMIN 전용 엔드포인트 접근 |
| `404` | `CHAT_NOT_FOUND` | 존재하지 않는 Chat ID |
| `409` | `FEEDBACK_ALREADY_EXISTS` | 동일 사용자 + 동일 대화 피드백 중복 생성 시도 |

---

## 2. 피드백 생성

> 인증 필요 — `MEMBER`(자신의 대화만), `ADMIN`(모든 대화)

```
POST /api/feedbacks
```

### Request Body

```json
{
  "chatId": 42,
  "isPositive": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| chatId | Long | Y | 피드백 대상 Chat ID (개별 Q&A 쌍) |
| isPositive | Boolean | Y | `true` = 긍정, `false` = 부정 |

### Response `201 Created`

```json
{
  "feedbackId": 1,
  "chatId": 42,
  "isPositive": true,
  "status": "PENDING",
  "createdAt": "2026-07-14T10:10:00+09:00"
}
```

### 에러 케이스

| 상황 | Status | code |
|---|---|---|
| chatId 누락 / isPositive 누락 | `400` | `VALIDATION_ERROR` |
| 존재하지 않는 chatId | `404` | `CHAT_NOT_FOUND` |
| 본인 소유 대화가 아님 (MEMBER) | `403` | `FORBIDDEN` |
| 동일 대화에 이미 피드백 존재 | `409` | `FEEDBACK_ALREADY_EXISTS` |

---

## 3. 피드백 목록 조회

> 인증 필요 — `MEMBER`(자신이 작성한 피드백만), `ADMIN`(전체)

```
GET /api/feedbacks
```

### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| page | Integer | N | `0` | 페이지 번호 (0-based) |
| size | Integer | N | `20` | 페이지 크기 |
| direction | String | N | `DESC` | 정렬 방향 (`ASC` / `DESC`) — 기준 컬럼은 `createdAt` 고정 |
| isPositive | Boolean | N | — | 생략 시 전체. `true` = 긍정만, `false` = 부정만 |

### Response `200 OK`

```json
{
  "content": [
    {
      "feedbackId": 1,
      "chatId": 42,
      "userId": 3,
      "isPositive": true,
      "status": "PENDING",
      "createdAt": "2026-07-14T10:10:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

- `userId` 필드는 `ADMIN`에게만 노출된다. `MEMBER` 응답에는 포함하지 않는다.

---

## 4. 피드백 상태 변경

> 인증 필요 — `ADMIN` 전용

```
PATCH /api/feedbacks/{feedbackId}/status
```

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| feedbackId | Long | Y | 상태를 변경할 피드백 ID |

### Request Body

```json
{
  "status": "RESOLVED"
}
```

| 필드 | 타입 | 필수 | 허용값 | 설명 |
|---|---|---|---|---|
| status | String | Y | `PENDING`, `RESOLVED` | 변경할 상태 |

### Response `200 OK`

```json
{
  "feedbackId": 1,
  "status": "RESOLVED"
}
```

### 에러 케이스

| 상황 | Status | code |
|---|---|---|
| 존재하지 않는 feedbackId | `404` | `FEEDBACK_NOT_FOUND` |
| MEMBER가 이 엔드포인트 호출 | `403` | `FORBIDDEN` |
| 허용되지 않은 status 값 | `400` | `VALIDATION_ERROR` |

---

## 5. 권한 (Authorization)

### 역할 정의

기존 `MEMBER` / `ADMIN` 역할을 그대로 사용한다. [사용자인증 api.md](../사용자인증-feat-user-auth/api.md) 참조.

### 엔드포인트별 접근 권한

| Method | Path | 인증 필요 | MEMBER | ADMIN |
|---|---|---|---|---|
| POST | `/api/feedbacks` | Y | 자신의 대화에만 생성 | 모든 대화에 생성 |
| GET | `/api/feedbacks` | Y | 자신이 작성한 피드백만 조회 | 전체 피드백 조회 |
| PATCH | `/api/feedbacks/{feedbackId}/status` | Y | 불가 (`403`) | 모든 피드백 상태 변경 |

---

## 6. 설계 결정 기록 (Decision Log)

| # | 질문 | 선택 | 선택지 후보 | 이유 |
|---|---|---|---|---|
| 1 | 피드백 대상 ID 필드명 | `chatId` (chats.id) | ① `chatId` ② `threadId` | 개별 Q&A 쌍 단위로 확정. 사용자 확인 완료. |
| 2 | 목록 응답에서 userId 노출 범위 | ADMIN에게만 노출 | ① 항상 노출 ② ADMIN에게만 | MEMBER는 자신의 피드백만 조회하므로 userId 불필요. ADMIN은 작성자 식별 필요. |
| 3 | 상태 변경 엔드포인트 메서드 | `PATCH` | ① PATCH ② PUT | 리소스 전체가 아닌 status 단일 필드만 변경하므로 PATCH가 의미상 적합. |
| 4 | ADMIN 목록 조회 시 userId 필터 | Out-of-scope | ① 없음 ② ?userId= 파라미터 추가 | 사용자 확인 완료 — 불필요. |
