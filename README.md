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
| [002-2](https://github.com/zoek1211/rest-api-2/tree/002-2) | 엔티티와 샘플 데이터 | 공통 부모 `BaseEntity`(id, 생성일, 수정일), 글 `Post`, 댓글 `PostComment` 엔티티와 리포지터리·서비스를 만들고, 앱 시작 시 글 3건·댓글 5건을 넣는 `BaseInitData` 를 추가했다. 글이 이미 있으면 넣지 않는다. | `@MappedSuperclass` 와 JPA Auditing, `@OneToMany(mappedBy)`/`@ManyToOne(LAZY)` 양방향 관계와 cascade·orphanRemoval, 댓글을 `Post.addComment()` 로 글을 통해 추가하는 이유(삭제도 006 단계에서 글을 통해 함), id 기반 equals 에서 저장 전(id 0) 엔티티 처리, `@Transactional` 프록시와 self 주입(`@Lazy`), `ApplicationRunner`. H2 콘솔(`http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:./db_dev;MODE=MySQL`, 사용자 `sa`)에서 데이터 확인. |
| [003-1](https://github.com/zoek1211/rest-api-2/tree/003-1) | 공통 응답·오류 처리와 글 작성 테스트 (실패) | 공통 응답 `RsData`, 상태코드를 맞추는 `ResponseAspect`, 전역 예외 처리 `GlobalExceptionHandler`(404/400)를 만들고, 글 작성 테스트 2개(성공, 제목 없음 400)를 먼저 썼다. 아직 API 가 없어서 두 테스트 모두 **실패**한다(404, 새 글 없음). | TDD 의 빨강 단계: 구현 전에 "어떻게 동작해야 하는지"를 테스트로 먼저 적는다. `@SpringBootTest`+MockMvc+`@Transactional` 롤백 테스트, `jsonPath` 로 응답 검사, record·제네릭, AOP `@Around`, `@ControllerAdvice`. |
| [003-2](https://github.com/zoek1211/rest-api-2/tree/003-2) | 글 작성 구현 | `POST /api/v1/posts` 를 만들었다(`ApiV1PostController.write`, 응답용 `PostDto`). 003-1 의 테스트 2개가 **통과**한다. 성공은 201 `201-1`, 제목 없음·글자 수 위반은 400 `400-1`, 잘못된 JSON 은 400 "요청 본문이 올바르지 않습니다." | TDD 의 초록 단계: 테스트를 통과시킬 만큼만 구현. `@RequestBody`+`@Valid` 와 `@NotBlank`/`@Size` 검증, 엔티티 대신 DTO 로 응답하는 이유(양방향 관계 무한 루프, 응답 형식 분리). |
| [004-1](https://github.com/zoek1211/rest-api-2/tree/004-1) | 글 삭제 테스트 (실패) | 글 삭제 테스트 2개를 먼저 썼다: 1번 글(댓글 3건)을 지우면 200 이고 글과 댓글이 모두 사라지는지, 없는 999번 글을 지우면 404 `404-1` 인지. 아직 API 가 없어서 **실패**한다. | 샘플 데이터를 테스트의 "given" 으로 쓰기, `EntityManager`+JPQL 로 댓글 수 세기(쿼리 전 자동 flush), `@PathVariable` 로 들어갈 주소 설계, static import 와 메서드 이름 충돌. |
