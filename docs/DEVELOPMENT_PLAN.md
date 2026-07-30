# Next Campus (LOST-INHA) 고도화 계획

BE-V2 레포 기준 개발 로드맵. 팀 논의로 정리된 항목을 우선순위와 함께 정리하고, AI 툴링(Skill/Harness/Hook/MCP) 도입 방법을 구체화한다.

## 우선순위 제안

항목이 서로 의존관계가 있어서 아래 순서를 권장한다.

1. **Infra 기반 전환** — Storage(Firebase) → DB(Postgres) → Docker-compose → Render CI/CD 순으로, 배포 인프라가 먼저 안정돼야 나머지 작업을 그 위에서 검증할 수 있음
2. **Test** — 인프라 전환 직후 회귀 방지용 테스트부터 채워야 이후 Code/Performance 작업의 안전망이 생김
3. **Code** — 에러 포맷 통일 → 기능 개발(OAuth, 푸시 알림, 댓글) 순
4. **Performance** — 트래픽이 실제로 생긴 뒤 측정 기반으로 진행 (조기 최적화 지양)
5. **AI 워크플로우** — 위 작업들과 병행해서 계속 다듬어가는 항목 (아래 별도 정리)

---

## Infra

- [x] Storage를 Firebase Storage로 전환 (`WebConfig.java`의 로컬 디스크 업로드 → Firebase Storage URL 저장으로 변경)
- [ ] DB를 PostgreSQL로 전환 (MySQL 드라이버/dialect 교체, JPA 쿼리 중 MySQL 종속 문법 점검)
- [x] Docker-compose 구성 (로컬 개발용: API + Postgres + Redis)
- [x] Redis 추가 (JWT 리프레시 토큰 저장/블랙리스트, 캐싱 용도)
- [ ] CI/CD를 Render 배포로 전환 (GitHub Actions → Render Deploy Hook 연동), Render는 PostgreSQL만 지원하므로 위 DB 전환이 선행 조건

## Code

- [ ] 에러 메시지 리팩토링 — 현재 `RestExceptionHandler`가 `global/auth/token/exception` 패키지에 한정되어 있음. 전역 `@RestControllerAdvice`로 확장
- [ ] 응답 포맷 통일 — 공통 `ApiResponse<T>` 래퍼 도입해 성공/실패 응답 스키마 일치
- [ ] OAuth 개발
- [ ] 푸시 알림 개발 (Firebase Cloud Messaging, 기존 `FcmService`/`FirebaseMessagingService` 확장)
- [ ] 댓글 기능 개발

## Test

- [ ] 테스트 코드 작성 — 현재 `src/test`에는 Gradle 기본 생성 `NextCampusApplicationTests`(contextLoads)만 존재. 인증/게시글 CRUD 등 핵심 도메인부터 단위·통합 테스트 작성
- [ ] 테스트 자동화 구축 — GitHub Actions에서 PR 시 자동 실행되도록 CI 게이트 연결

## Performance

- [ ] 성능 테스트
  - [ ] DB 성능 튜닝 — 인덱스 설계, `EXPLAIN`으로 쿼리 개선, 외부 호출(Firebase, 메일 발송 등) 동기/비동기 전환
  - [ ] JVM 튜닝 — GC 종류/비율 조정
  - [ ] `tcpdump` 패킷 분석 기반 장애 해결 훈련
  - [ ] `k6`로 properties/커넥션 풀 설정 부하 테스트
  - [ ] 스레드 풀 수치 튜닝
  - [ ] Tomcat → Netty 전환 검토

---

## AI 워크플로우 구축 (Skill / Harness / Hook / MCP)

Claude Code를 팀의 반복 작업(배포, PR, DB 마이그레이션, 코드 리뷰)에 맞춰 커스터마이징하는 부분. 개인 설정이 아니라 팀 표준으로 굳히는 게 목적이므로, 레포에 커밋해서 팀원 전원이 같은 방식으로 Claude Code를 쓰게 만드는 것이 핵심이다.

### 0. 저장 위치 원칙

| 대상 | 위치 | 커밋 여부 |
|---|---|---|
| 팀 공용 규칙/설정 | `.claude/settings.json`, `CLAUDE.md`, `.claude/skills/**`, `.mcp.json` | 커밋 (레포에 포함) |
| 개인 전용 설정, 로컬 시크릿 | `.claude/settings.local.json` | `.gitignore` 처리 |

`CLAUDE.md`를 레포 루트에 만들어 도메인 패키지 구조(`domain/{category,comment,location,member,notification,post,receiver}`), 커밋 컨벤션, PR 템플릿(`.github/pull_request_template.md`) 요구사항을 적어두면 매 세션마다 같은 설명을 반복할 필요가 없어진다.

### 1. Skills — 반복 작업의 절차화

Skill은 `.claude/skills/<이름>/SKILL.md`에 YAML frontmatter + 지침을 작성해두는 패키지다. `/이름`으로 직접 호출하거나, description이 현재 요청과 맞으면 자동으로 제안된다.

```markdown
---
name: deploy
description: Render로 배포하기 전 체크리스트 실행 후 배포 트리거. "배포해줘", "deploy" 요청 시 사용.
---

1. `./gradlew test`로 테스트 통과 확인
2. `application-prod.properties`에 시크릿이 하드코딩되지 않았는지 확인
3. Render Deploy Hook URL로 POST 요청 (또는 main 브랜치 push로 auto-deploy 트리거)
4. 배포 후 `/actuator/health` 엔드포인트로 헬스체크 확인
```

이 프로젝트에 우선 만들어두면 좋은 Skill 후보:

- **`deploy`** — 위 예시. Render 배포 전 체크리스트 + 헬스체크
- **`new-domain`** — `category`/`post` 같은 기존 도메인 패키지 구조(Controller/DTO/Model/Repository/Service)를 스캐폴딩하는 절차. 새 도메인 추가할 때마다 구조를 통일
- **`db-migrate`** — MySQL→Postgres 전환처럼 스키마 변경 시 dialect/드라이버/DDL 점검 체크리스트
- **`pr-check`** — PR 템플릿 항목(커밋 컨벤션, 테스트 여부) 자동 검증 후 PR 설명 초안 작성

### 2. Harness — Skill을 조합해 팀 전용 워크플로우로 굳히기

"하네스"는 단일 Skill이 아니라, `CLAUDE.md` + 여러 Skill + Hook + MCP 설정이 합쳐져서 "이 레포에서 Claude Code가 항상 이렇게 동작한다"는 하나의 일관된 시스템을 이루는 것을 말한다. 개별 Skill을 하나씩 추가하는 게 아니라:

1. `CLAUDE.md`에 프로젝트 규칙(패키지 구조, 브랜치 전략 `dev1`/`dev2`/`dev3` → `main`, 커밋 컨벤션)을 기술
2. 반복 작업 단위로 Skill을 쪼개서 `.claude/skills/`에 축적
3. 위험 동작은 Hook으로 자동 차단 (아래 3번)
4. 외부 시스템 접근은 MCP로 표준화 (아래 4번)

이 네 가지가 레포에 커밋되어 있으면, 팀원 누구든 Claude Code를 켜는 순간 동일한 "팀 전용 어시스턴트"가 동작하게 된다. 처음부터 완성형으로 만들려 하지 말고, 실제로 반복하게 되는 작업이 보일 때마다 Skill 하나씩 추가하는 방식을 권장한다 (과설계 방지).

### 3. Hooks — 권한/안전 가드

Hook은 `.claude/settings.json`에 정의하며, 특정 도구 호출 전후로 셸 명령을 실행해 자동으로 막거나 확인시킬 수 있다. 이 프로젝트에서 실제로 걸어둘 만한 가드:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "grep -qE 'application(-prod)?\\.properties$|firebase/.*\\.json$|\\.env$' <<< \"$CLAUDE_TOOL_INPUT_FILE_PATH\" && echo 'BLOCKED: 시크릿 파일 직접 수정 금지' >&2 && exit 1 || exit 0"
          }
        ]
      },
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "grep -qE 'push.*(origin main|--force)' <<< \"$CLAUDE_TOOL_INPUT_COMMAND\" && echo 'BLOCKED: main 직접 push/force push 금지' >&2 && exit 1 || exit 0"
          }
        ]
      }
    ]
  }
}
```

우선순위로 걸어둘 가드:

- `application*.properties`, `firebase/*.json`, `.env` 등 시크릿 파일 직접 편집/커밋 차단
- `main` 브랜치 직접 push, `--force` push 차단 (PR 경유 강제)
- `git reset --hard`, `rm -rf` 같은 파괴적 명령은 실행 전 확인 필수화

정확한 훅 스키마와 사용 가능한 이벤트 목록은 프로젝트에서 `update-config` skill로 설정하는 걸 권장 (스키마가 버전에 따라 바뀔 수 있음).

### 4. MCP 서버 — 외부 툴 연동

MCP(Model Context Protocol) 서버를 붙이면 Claude Code가 외부 시스템을 직접 조회/조작할 수 있다. README에 이미 명시된 툴 체인(Notion, Figma, Postman, GitHub) 기준으로 우선순위:

- **GitHub MCP** — 이슈/PR 조회, 리뷰 코멘트 자동화. `gh` CLI로도 상당 부분 대체 가능하니 필요성부터 판단
- **Notion MCP** — 기획 문서/회의록과 코드 작업 연결 (README의 Notion 배지 활용)
- **DB MCP (Postgres)** — Render 배포 후 운영 DB 스키마 확인/쿼리 디버깅용. **반드시 read-only 계정으로 연결** — 운영 DB에 쓰기 권한을 MCP에 주지 않는다
- **Slack MCP** — 배포 완료/실패, 에러 알림을 팀 채널로 연동 (팀에서 Slack 쓴다면)

추가 명령 예시:

```bash
claude mcp add github --transport http https://api.githubcopilot.com/mcp/
claude mcp add postgres-readonly -- npx -y @modelcontextprotocol/server-postgres "postgresql://readonly_user:***@render-host/dbname"
```

DB MCP는 특히 주의: 쓰기 권한 계정을 연결하면 Claude가 실수로 운영 데이터를 수정할 수 있으므로, 별도 read-only 유저를 만들어서 연결하고 스키마 변경은 항상 마이그레이션 파일 + PR 리뷰를 거치도록 강제한다.

---

## 다음 액션

1. Infra: Firebase Storage 전환 → Postgres 전환 → Docker-compose → Render CI/CD 순으로 진행
2. AI 워크플로우: `CLAUDE.md` 작성 → `deploy`/`new-domain` Skill부터 추가 → 시크릿 파일 보호 Hook 우선 적용 → 필요해지면 MCP 추가
