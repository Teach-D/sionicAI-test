# API 명세 — 사용자 관리 및 인증

- **브랜치**: `feat/user-auth`
- **작성일**: 2026-07-14
- **Base URL**: `/api`

---

## 1. 공통

### 인증 방식

모든 보호 엔드포인트는 요청 헤더에 JWT 토큰을 포함해야 한다.

```
Authorization: Bearer <accessToken>
```

### 공통 에러 응답 형식

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "필드별 검증 오류 메시지",
  "timestamp": "2026-07-14T10:00:00+09:00"
}
```

### 공통 에러 코드

| HTTP Status | code | 발생 상황 |
|---|---|---|
| `400` | `VALIDATION_ERROR` | 요청 바디 유효성 검사 실패 |
| `401` | `UNAUTHORIZED` | 토큰 없음·만료·유효하지 않음 |
| `403` | `FORBIDDEN` | 권한 부족 |
| `409` | `DUPLICATE_EMAIL` | 이미 사용 중인 이메일 |

---

## 2. 인증 (Authentication)

### 2.1 회원가입

> 인증 불필요

```
POST /api/auth/register
```

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| email | String | Y | 이메일 형식 |
| password | String | Y | 최소 8자 |
| name | String | Y | 공백 불가 |

**Response `201 Created`**

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "MEMBER",
  "createdAt": "2026-07-14T10:00:00+09:00"
}
```

**에러 케이스**

| 상황 | Status | code |
|---|---|---|
| 이메일 중복 | `409` | `DUPLICATE_EMAIL` |
| 유효성 검사 실패 | `400` | `VALIDATION_ERROR` |

---

### 2.2 로그인

> 인증 불필요

```
POST /api/auth/login
```

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| email | String | Y | 이메일 형식 |
| password | String | Y | 공백 불가 |

**Response `200 OK`**

```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer"
}
```

**에러 케이스**

| 상황 | Status | code |
|---|---|---|
| 이메일 미존재 또는 비밀번호 불일치 | `401` | `UNAUTHORIZED` |

> 이메일 미존재와 비밀번호 불일치를 동일 메시지로 처리한다 (계정 존재 여부 노출 방지).

---

## 3. 권한 (Authorization)

### 역할 정의

| Role | 설명 |
|---|---|
| `MEMBER` | 일반 사용자. 회원가입 시 자동 부여. |
| `ADMIN` | 관리자. DB에서 직접 설정. |

### 엔드포인트별 접근 권한

| Method | Path | 인증 필요 | 허용 Role |
|---|---|---|---|
| POST | `/api/auth/register` | N | 전체 |
| POST | `/api/auth/login` | N | 전체 |
| 그 외 모든 경로 | — | Y | `MEMBER`, `ADMIN` |

### JWT 토큰 명세

| 항목 | 값 |
|---|---|
| 알고리즘 | HS256 |
| Subject | 사용자 email |
| Claims | `role` |
| 만료 시간 | 1시간 |
| Refresh Token | 미지원 — 만료 시 재로그인 |

### 인증 처리 흐름

```
클라이언트 요청
    │
    ▼
JwtAuthenticationFilter
    ├── Authorization 헤더 없음  →  401 UNAUTHORIZED
    ├── 토큰 파싱 실패 / 만료    →  401 UNAUTHORIZED
    └── 검증 성공
            │
            ▼
        SecurityContext에 인증 정보 설정
            │
            ▼
        Controller 진입
```
