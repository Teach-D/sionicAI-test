# sionic-ai Chatbot API — CLAUDE.md

AI 챗봇 서비스 백엔드 API 프로젝트.
VIP onboarding 팀의 고객사 시연용으로 시작되었으나, 지속적 확장을 전제로 한다.
Claude Code가 이 저장소에서 작업할 때 반드시 이 문서를 먼저 확인한다.

---

## 절대 규칙 (위반 금지)

1. **코딩 전에 먼저 생각한다**
   - 가정은 숨기지 않고 명시한다. 불확실하면 코드보다 질문이 먼저다.
   - 해석이 여럿이면 조용히 하나 고르지 말고 둘 다 제시하고 고르게 한다.
   - 더 단순한 방법이 보이면 말하고, 필요하면 반대 의견을 낸다.
   - 무엇이 헷갈리는지 이름 붙여 묻는다. "잘 모르겠습니다"는 금지.

2. **최소 코드 원칙** — 지금 실패하는 테스트를 통과시키는 데 필요한 최소한만 작성한다.
   - 요청하지 않은 방어 로직·추상화·유틸·설정 가능성·유연성 미리 만들기 금지.
   - "나중에 쓸 것 같아서" 금지. 필요해지는 순간 그 테스트와 함께 추가한다.
   - single-use 코드에 추상화 금지.
   - 판단 기준: "시니어 엔지니어가 과하다고 할까?" → 그렇다면 단순화.

3. **비목표 우선**: 모든 기능 작업은 목표뿐 아니라 "하지 않을 것(Non-goals)"을 먼저 확인한다.
   범위 밖 파일·엔드포인트·기능을 만들지 않는다.

4. **외과적 변경** — 기존 코드를 건드릴 때 특히 적용.
   - 건드려야 하는 것만 건드린다. 인접 코드·주석·포맷을 "개선"하지 않는다.
   - 관련 없는 죽은 코드를 발견하면 삭제하지 말고 언급만 한다.
   - 내 변경으로 안 쓰이게 된 import/변수/함수만 정리한다.

5. **보안 우선**: 회원가입·로그인을 제외한 모든 엔드포인트는 JWT 인증을 통과해야 한다.
   인증 없이 접근 가능한 엔드포인트를 추가할 때는 반드시 명시적으로 확인받는다.

6. **환경 분리**: 환경별 설정은 `application-{profile}.yml`로만 관리한다.
   하드코딩된 DB URL·시크릿·API 키는 절대 커밋하지 않는다.

---

## 기술 스택

| 분류 | 선택 |
|---|---|
| 언어 | Kotlin 2.1, JVM 21 |
| 프레임워크 | Spring Boot 3.4 |
| 빌드 | Gradle Kotlin DSL |
| 인증 | Spring Security + JWT (jjwt 0.12) |
| ORM | Spring Data JPA + Hibernate |
| DB (dev) | H2 in-memory (`MODE=PostgreSQL`) |
| DB (prod) | PostgreSQL |
| AI | OpenAI API (Chat Completions) |

---

## 프로젝트 구조

```
src/main/kotlin/com/sionic/app/
├── SionicApplication.kt
├── config/          # Security, OpenAI bean, JPA auditing 등 설정
├── security/        # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsService
└── domain/
    ├── user/        # User 엔티티, Repository, Service, Controller
    ├── chat/        # Thread, Chat 엔티티, Repository, Service, Controller
    ├── feedback/    # Feedback 엔티티, Repository, Service, Controller
    └── report/      # 활동 기록 및 CSV 보고서 (관리자 전용)
```

패키지는 도메인 단위로 분리한다. 도메인 간 의존은 Service 레이어를 통해서만 한다.

---