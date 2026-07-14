# API 명세 — 대화(Chat) 관리

- **브랜치**: `feat/chat`
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
| `403` | `FORBIDDEN` | 타 유저 소유 리소스 접근 시도 |
| `404` | `THREAD_NOT_FOUND` | 존재하지 않는 스레드 ID |
| `502` | `OPENAI_ERROR` | OpenAI API 호출 실패 |

---

## 2. 대화 생성

> 인증 필요 — `MEMBER`, `ADMIN`

```
POST /api/chats
```

### Request Body

```json
{
  "question": "스프링 부트란 무엇인가요?",
  "isStreaming": false,
  "model": "gpt-4o-mini"
}
```

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| question | String | Y | — | 사용자 질문 (공백 불가) |
| isStreaming | Boolean | N | `false` | `true`이면 SSE 스트리밍 응답 |
| model | String | N | `gpt-4o-mini` | OpenAI 모델명 |

### Response — 일반 응답 (`isStreaming: false`)

**`201 Created`**

```json
{
  "chatId": 42,
  "threadId": 7,
  "question": "스프링 부트란 무엇인가요?",
  "answer": "스프링 부트는 ...",
  "createdAt": "2026-07-14T10:05:00+09:00"
}
```

### Response — 스트리밍 응답 (`isStreaming: true`)

**`200 OK`** — Content-Type: `text/event-stream`

```
data: 스프링
data:  부트는
data:  자바 기반의
data: ...
data: [DONE]
```

- 각 `data:` 이벤트는 OpenAI로부터 수신한 토큰 청크.
- 스트리밍 완료 후 전체 답변을 DB에 저장한다.
- 스트리밍 도중 오류 발생 시 `data: [ERROR]` 이벤트를 전송하고 스트림 종료.

### 스레드 자동 결정 로직

```
대화 생성 요청 수신
    │
    ▼
유저의 최근 스레드 조회 (last_chat_at DESC)
    ├── 스레드 없음           → 새 스레드 생성
    ├── last_chat_at ≥ 30분  → 새 스레드 생성
    └── last_chat_at < 30분  → 기존 스레드 재사용
            │
            ▼
        해당 스레드의 전체 대화 조회 → OpenAI messages 구성
            │
            ▼
        OpenAI Chat Completions 호출
            │
            ▼
        Chat 저장 + Thread.last_chat_at 갱신
```

### 에러 케이스

| 상황 | Status | code |
|---|---|---|
| question 공백 또는 누락 | `400` | `VALIDATION_ERROR` |
| OpenAI API 호출 실패 | `502` | `OPENAI_ERROR` |

---

## 3. 스레드·대화 목록 조회

> 인증 필요 — `MEMBER`(자신), `ADMIN`(전체)

```
GET /api/threads
```

### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| page | Integer | N | `0` | 페이지 번호 (0-based) |
| size | Integer | N | `20` | 페이지 크기 |
| sort | String | N | `createdAt` | 정렬 기준 컬럼 (`createdAt` 고정) |
| direction | String | N | `DESC` | 정렬 방향 (`ASC` / `DESC`) |

- 정렬 기준은 **스레드의 `createdAt`**.
- 각 스레드 내 대화(chats)는 항상 `createdAt ASC` 고정.
- `MEMBER`는 자신의 스레드만 조회된다. `ADMIN`은 전체 조회.

### Response `200 OK`

```json
{
  "content": [
    {
      "threadId": 7,
      "createdAt": "2026-07-14T10:00:00+09:00",
      "chats": [
        {
          "chatId": 42,
          "question": "스프링 부트란 무엇인가요?",
          "answer": "스프링 부트는 ...",
          "createdAt": "2026-07-14T10:05:00+09:00"
        }
      ]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 4. 스레드 삭제

> 인증 필요 — `MEMBER`(자신 소유만), `ADMIN`(자신 소유만)

```
DELETE /api/threads/{threadId}
```

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| threadId | Long | Y | 삭제할 스레드 ID |

### Response `204 No Content`

응답 바디 없음.

### 에러 케이스

| 상황 | Status | code |
|---|---|---|
| 존재하지 않는 threadId | `404` | `THREAD_NOT_FOUND` |
| 본인 소유가 아닌 스레드 | `403` | `FORBIDDEN` |

---

## 5. 권한 (Authorization)

### 역할 정의

기존 `MEMBER` / `ADMIN` 역할을 그대로 사용한다. [사용자인증 api.md](../사용자인증-feat-user-auth/api.md) 참조.

### 엔드포인트별 접근 권한

| Method | Path | 인증 필요 | MEMBER | ADMIN |
|---|---|---|---|---|
| POST | `/api/chats` | Y | 자신의 대화 생성 | 자신의 대화 생성 |
| GET | `/api/threads` | Y | 자신의 스레드+대화만 | 전체 스레드+대화 |
| DELETE | `/api/threads/{threadId}` | Y | 자신의 스레드만 | 자신의 스레드만 |
