# 기능 명세서 — 사용자 관리 및 인증

- **브랜치**: `feat/user-auth`
- **작성일**: 2026-07-14

---

## 1. 개요

사용자 회원가입·로그인 및 JWT 기반 인증 기능을 제공한다.
회원가입·로그인 엔드포인트를 제외한 모든 API는 JWT 토큰 인증을 필수로 요구한다.

---

## 2. 도메인 모델

엔티티 필드·DB 타입·인덱스는 [erd.md](./erd.md) 를 단일 출처로 한다.

**권한 부여 정책**
- 회원가입 시 role은 항상 `MEMBER`로 고정 (요청 바디에서 role 수신 불가).
- `ADMIN` 계정은 DB에서 직접 변경한다.

---

## 3. API 명세 및 권한

엔드포인트 상세·요청/응답 스키마·에러 코드·JWT 명세·권한 테이블은 [api.md](./api.md) 를 단일 출처로 한다.

---

## 4. 구현 범위

### In-scope

- `User` 엔티티 및 `UserRepository`
- 회원가입 (`POST /api/auth/register`)
- 로그인 (`POST /api/auth/login`) — JWT 발급
- `JwtTokenProvider` — 토큰 생성·검증
- `JwtAuthenticationFilter` — 요청마다 토큰 검증
- `SecurityConfig` — whitelist 설정, 나머지 인증 필수 처리
- `GlobalExceptionHandler` — 인증/유효성 오류 일괄 처리

### Out-of-scope

- Refresh Token
- 비밀번호 변경·재설정
- 회원 탈퇴
- 소셜 로그인
- 이메일 인증
- 관리자 생성 API (DB 직접 설정으로 대체)

---

## 5. 비밀번호 정책

- 최소 8자 이상
- 그 외 조건(특수문자, 대소문자 혼합 등) 없음
- 저장 시 BCrypt로 해시 처리 (`PasswordEncoder`)

---

## 6. 설계 결정 기록 (Decision Log)

| # | 질문 | 선택 | 선택지 후보 | 이유 |
|---|---|---|---|---|
| 1 | 회원가입 시 role 부여 방식 | 기본값 `MEMBER` 고정, `ADMIN`은 DB 직접 설정 | ① DB 직접 설정 ② 가입 시 role 파라미터 수신 ③ 별도 admin 생성 API | 가입 API에서 role을 수신하면 누구나 ADMIN으로 가입 가능한 보안 취약점이 생김. 시연 범위에서 admin 생성 API는 과함. |
| 2 | JWT Refresh Token 지원 여부 | Access Token만 발급, 만료 시 재로그인 | ① Access Token만 ② Access + Refresh Token | 시연 목적의 3시간 과제 범위에서 Refresh Token은 구현 복잡도 대비 효용이 낮음. 확장 시 추가 가능. |
| 3 | 비밀번호 정책 | 최소 8자 이상만 | ① 8자 이상만 ② 8자 + 영문/숫자/특수문자 조합 | 고객사 시연 단계에서 UX 마찰을 최소화. 정책 강화는 실서비스 전환 시 적용. |
