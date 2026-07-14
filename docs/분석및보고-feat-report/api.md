# API 명세 — 분석 및 보고

- **브랜치**: `feat/report`
- **작성일**: 2026-07-14
- **Base URL**: `/api/admin`

---

## 1. 공통

### 인증 방식

모든 엔드포인트는 JWT 인증이 필요하며, `ADMIN` 역할만 접근 가능하다.

```
Authorization: Bearer <accessToken>
```

### 공통 에러 응답 형식

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "오류 메시지",
  "timestamp": "2026-07-14T10:00:00+09:00"
}
```

### 공통 에러 코드

| HTTP Status | code | 발생 상황 |
|---|---|---|
| `401` | `UNAUTHORIZED` | 토큰 없음·만료·유효하지 않음 |
| `403` | `FORBIDDEN` | ADMIN 역할이 아닌 사용자 접근 시도 |

---

## 2. 사용자 활동 기록 조회

> 인증 필요 — `ADMIN` 전용

```
GET /api/admin/activity
```

### Query Parameters

없음. 집계 범위는 요청 시점 기준 직전 24시간(rolling window) 고정.

### Response `200 OK`

```json
{
  "from": "2026-07-13T10:00:00+09:00",
  "to": "2026-07-14T10:00:00+09:00",
  "signUpCount": 5,
  "loginCount": 23,
  "chatCreatedCount": 47
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| from | String (ISO 8601) | 집계 시작 시각 (요청 시각 − 24h) |
| to | String (ISO 8601) | 집계 종료 시각 (요청 시각) |
| signUpCount | Integer | 해당 기간 내 회원가입 수 |
| loginCount | Integer | 해당 기간 내 로그인 수 |
| chatCreatedCount | Integer | 해당 기간 내 대화 생성 수 |

---

## 3. CSV 보고서 생성

> 인증 필요 — `ADMIN` 전용

```
GET /api/admin/report/csv
```

### Query Parameters

없음. 보고서 범위는 요청 시점 기준 직전 24시간(rolling window) 고정.

### Response `200 OK`

```
Content-Type: text/csv; charset=UTF-8
Content-Disposition: attachment; filename="report-{yyyy-MM-dd}.csv"
```

**CSV 구조 (헤더 포함)**

```csv
chatId,threadId,userId,userEmail,userName,question,answer,createdAt
42,7,1,user@example.com,홍길동,스프링 부트란?,스프링 부트는 ...,2026-07-14T10:05:00+09:00
```

| 컬럼 | 설명 |
|---|---|
| chatId | 대화 ID |
| threadId | 스레드 ID |
| userId | 사용자 ID |
| userEmail | 사용자 이메일 |
| userName | 사용자 이름 |
| question | 질문 내용 |
| answer | AI 답변 내용 |
| createdAt | 대화 생성 일시 (ISO 8601) |

- 해당 기간 내 대화가 없으면 헤더 행만 포함된 CSV를 반환한다.
- 정렬 기준: `createdAt ASC`.

---

## 4. 권한 (Authorization)

### 역할 정의

기존 `MEMBER` / `ADMIN` 역할을 그대로 사용한다. [사용자인증 api.md](../사용자인증-feat-user-auth/api.md) 참조.

### 엔드포인트별 접근 권한

| Method | Path | 인증 필요 | MEMBER | ADMIN |
|---|---|---|---|---|
| GET | `/api/admin/activity` | Y | 접근 불가 (403) | 전체 집계 조회 |
| GET | `/api/admin/report/csv` | Y | 접근 불가 (403) | CSV 보고서 다운로드 |
