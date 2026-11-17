# 작업지침

- 한국어 사용
- 일반적인 작업 흐름 : /grill-with-docs, /to-spec, /to-tickets, /implement
  - /implement 는 내부적으로 /tdd 로 구현하고 커밋 전에 /code-review 를 수행
- 버그 발생 시 /diagnosing-bugs, 아키텍처 개선이 필요할 때만 /improve-codebase-architecture
- 최대한 mattpocock 스킬들을 활용

## 커밋 번호와 README 학습 기록

- 모든 커밋에 `000-1` 형식의 번호를 붙인다. 앞 세 자리는 작업 단위(티켓) 번호이고, 뒤 숫자는 그 단위 안의 커밋 순번이다.
  - 첫 커밋은 `000-1` (초기 설정), 이어서 `000-2`, `000-3` …
  - 새 티켓 작업을 시작하면 앞 번호를 올린다: `001-1`, `001-2` …
- 커밋 메시지 제목은 번호로 시작한다. 예: `000-1 프로젝트 초기 설정`
- 커밋할 때마다 같은 번호로 git 태그를 만들어 push 한다 (`git tag 000-1 && git push origin 000-1`).
  - 그러면 `https://github.com/zoek1211/rest-api-2/tree/000-1` 처럼 주소 끝의 번호만 바꿔서 해당 단계의 코드로 바로 이동할 수 있다.
- 커밋할 때마다 `README.md` 의 "커밋 단계별 설명" 표에 한 줄을 추가한다: 번호(태그 링크), 제목, 이 단계에서 무엇을 왜 했는지, 배울 점.
  - README 갱신은 그 커밋 안에 함께 포함한다.
- 표와 함께, README 의 "커밋별 상세 설명 (핵심 코드와 함께)" 섹션에도 커밋마다 소제목(`### [번호](태그 링크) 제목`)을 하나 추가한다.
  - 그 커밋에서 무엇을 왜 했는지 한두 줄
  - 그 커밋의 핵심 코드 발췌(주석 포함, 중요한 부분만)
  - 필요하면 요청/응답 예
  - `> 배울 점:` 한 줄

## 코드 주석

- 코드를 읽고 공부할 수 있도록 자세한 한국어 주석을 단다.
  - 파일 맨 위: 이 파일의 역할과 다른 파일과의 관계
  - 함수·클래스: 무엇을 하는지, 왜 이렇게 설계했는지, 입력과 출력
  - 처음 보는 사람이 헷갈릴 만한 줄: 문법이나 라이브러리 동작 설명
- 테스트 코드에도 이 테스트가 어떤 동작을 검증하는지 주석을 단다.

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues for zoek1211/rest-api-2, using the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the five default triage labels: needs-triage, needs-info, ready-for-agent, ready-for-human, wontfix. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one root `GLOSSARY.md` plus `docs/adr/`. See `docs/agents/domain.md`.
