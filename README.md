# rest-api-2

글과 댓글을 작성·삭제하는 게시판 REST API를 단계별로 만들며 Spring Boot, JPA, 테스트를 공부하는 프로젝트입니다.

- 참고 강의: https://www.slog.gg/p/14172/raw (원본 저장소 [jhs512/p-14172-2](https://github.com/jhs512/p-14172-2))
- 스펙: [#1](https://github.com/zoek1211/rest-api-2/issues/1)
- 용어: [GLOSSARY.md](GLOSSARY.md)

## 커밋 단계별 설명

각 커밋은 `000-1` 형식의 번호를 가지며, 같은 이름의 git 태그가 붙어 있습니다.
번호를 누르거나 주소 끝의 번호만 바꾸면 그 단계의 코드로 이동합니다:
`https://github.com/zoek1211/rest-api-2/tree/<번호>`

- 앞 세 자리: 작업 단위 번호 (000 설정, 001 프로젝트 생성, 002 엔티티·샘플 데이터, 003~006 기능)
- 뒤 숫자: 그 작업 단위 안의 커밋 순번 (`-1` 실패하는 테스트, `-2` 구현 처럼)

| 번호 | 제목 | 무엇을 / 왜 | 배울 점 |
| ---- | ---- | ----------- | ------- |
| [000-1](https://github.com/zoek1211/rest-api-2/tree/000-1) | 작업지침과 설정 문서 | 코드를 쓰기 전에 작업 규칙(CLAUDE.md), 이슈 트래커·라벨·도메인 문서 위치(docs/agents), 용어집(GLOSSARY.md)을 정했다. | 용어(글=Post, 댓글=PostComment)를 먼저 정해 두면 코드·테스트·이슈에서 같은 말을 쓰게 된다. |
| [001-1](https://github.com/zoek1211/rest-api-2/tree/001-1) | 프로젝트 생성 | start.spring.io 로 Spring Boot 4.1.2 / Java 25 / Gradle Kotlin DSL 프로젝트를 만들었다. 의존성은 webmvc, lombok, devtools 만. | `@SpringBootApplication` 이 하는 일(자동 설정 + 컴포넌트 스캔), Gradle wrapper(`./gradlew`)로 누구나 같은 Gradle 버전으로 빌드하는 법, `@SpringBootTest` 스모크 테스트. |
| [002-1](https://github.com/zoek1211/rest-api-2/tree/002-1) | 의존성 추가와 프로필 설정 | JPA, H2, validation, H2 콘솔 의존성을 추가하고 설정을 yaml 로 바꿨다. dev 프로필은 파일 DB(`db_dev`) + H2 콘솔, test 프로필은 메모리 DB. | 프로필로 개발/테스트 DB 를 나누는 이유(테스트가 개발 데이터를 건드리지 않게), `implementation`/`runtimeOnly`/`developmentOnly` 차이, `show-sql` 과 bind 로그로 JPA 가 만든 SQL 보기. |
