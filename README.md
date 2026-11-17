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
| [004-2](https://github.com/zoek1211/rest-api-2/tree/004-2) | 글 삭제 구현 | `DELETE /api/v1/posts/{id}` 를 만들었다(`PostService.findById`/`delete`, `ApiV1PostController.delete`). 004-1 의 테스트가 **통과**한다. 없는 글이면 `findById(...).get()` 의 예외를 전역 예외 처리가 404 로 바꾼다. 실제 앱에서도 1번 글을 지우면 댓글 1~3 이 함께 사라지는 것을 확인했다. | `@DeleteMapping` 과 `@PathVariable`, HTTP 메서드로 하는 일을 구분하는 REST 방식, `Optional` 과 `.get()` 의 예외, cascade REMOVE 가 부모보다 먼저 자식에게 전파되어 댓글이 먼저 지워지는 순서. |
| [005-1](https://github.com/zoek1211/rest-api-2/tree/005-1) | 댓글 작성 테스트 (실패) | 댓글 API 테스트 클래스를 새로 만들고 2개를 먼저 썼다: 1번 글에 댓글을 달면 201 이고 댓글이 4건이 되는지(응답 id 가 실제 저장된 id 인지), 없는 999번 글이면 404 인지. 아직 API 가 없어 **실패**한다. | 중첩 주소(`/posts/{postId}/comments`)로 "어느 글의 댓글인지" 표현하기, JPQL `order by ... desc` + `setMaxResults(1)` 로 최신 댓글 찾기. |

## 커밋별 상세 설명 (핵심 코드와 함께)

위 표의 각 번호를 조금 더 자세히, 그 커밋의 핵심 코드와 함께 설명합니다. 전체 코드는 번호 링크로 이동해서 보세요.

### [000-1](https://github.com/zoek1211/rest-api-2/tree/000-1) 작업지침과 설정 문서

코드보다 먼저 **규칙과 용어**를 정했습니다.

- `CLAUDE.md`: 작업 흐름(`/grill-with-docs → /to-spec → /to-tickets → /implement`), 커밋 번호 규칙, 주석 규칙
- `GLOSSARY.md`: 글(Post), 댓글(PostComment), 샘플 데이터

```markdown
**댓글 (PostComment)**:
하나의 글에 달린 짧은 글. 글 없이는 존재할 수 없고, 글이 삭제되면 함께 삭제된다.
```

> 배울 점: 용어를 먼저 정하면 클래스 이름, 테스트 이름, 이슈 제목이 모두 같은 말을 쓰게 됩니다.

### [001-1](https://github.com/zoek1211/rest-api-2/tree/001-1) 프로젝트 생성

start.spring.io 로 만든 빈 프로젝트입니다. 앱의 시작점은 이 한 클래스입니다.

```java
@SpringBootApplication   // = 설정 클래스 + 자동 설정 + 컴포넌트 스캔(com.back 이하)
public class BackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackApplication.class, args); // 컨테이너 생성 + 내장 톰캣(8080) 실행
    }
}
```

```java
@SpringBootTest   // 스프링 컨테이너 전체를 띄워 본다
class BackApplicationTests {
    @Test
    void contextLoads() { }   // 비어 있어도, 앱이 못 켜지면 실패
}
```

> 배울 점: `@SpringBootApplication` 은 최상위 패키지에 둬야 하위 패키지가 모두 스캔됩니다. `./gradlew` 는 누구나 같은 Gradle 버전으로 빌드하게 해 줍니다.

### [002-1](https://github.com/zoek1211/rest-api-2/tree/002-1) 의존성 추가와 프로필 설정

DB 를 쓰기 위한 의존성을 넣고, **개발용 DB 와 테스트용 DB 를 분리**했습니다.

```kotlin
implementation("org.springframework.boot:spring-boot-starter-data-jpa")   // JPA
implementation("org.springframework.boot:spring-boot-starter-validation") // @NotBlank 등
implementation("org.springframework.boot:spring-boot-h2console")          // /h2-console
runtimeOnly("com.h2database:h2")                                          // H2 DB
```

```yaml
# application-dev.yaml : 파일 DB, 껐다 켜도 데이터 유지
spring.datasource.url: jdbc:h2:./db_dev;MODE=MySQL
# application-test.yaml : 메모리 DB, 테스트가 끝나면 사라짐
spring.datasource.url: jdbc:h2:mem:db_test;MODE=MySQL
```

> 배울 점: 테스트는 `@ActiveProfiles("test")` 로 메모리 DB 를 쓰므로 개발 데이터를 건드리지 않습니다. `show-sql` 로 JPA 가 만든 SQL 을 볼 수 있습니다.

### [002-2](https://github.com/zoek1211/rest-api-2/tree/002-2) 엔티티와 샘플 데이터

**공통 부모** — 모든 엔티티의 id, 생성일, 수정일:

```java
@MappedSuperclass                                // 테이블은 아니고 필드만 물려줌
@EntityListeners(AuditingEntityListener.class)   // 날짜 자동 입력 (@EnableJpaAuditing 과 짝)
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = IDENTITY) private int id;
    @CreatedDate      private LocalDateTime createDate;
    @LastModifiedDate private LocalDateTime modifyDate;
}
```

**글과 댓글의 관계** — 글 1개 : 댓글 N개, 댓글은 글을 통해서만 만든다:

```java
public class Post extends BaseEntity {
    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<PostComment> comments = new ArrayList<>();

    public PostComment addComment(String content) {
        PostComment postComment = new PostComment(this, content);
        comments.add(postComment);   // 저장은 flush 때 cascade PERSIST 가 해 줌
        return postComment;
    }
}

public class PostComment extends BaseEntity {
    @ManyToOne(fetch = LAZY) private Post post;   // post_id 외래키, 필요할 때만 조회
}
```

**샘플 데이터** — 앱이 시작될 때 글 3건, 댓글 5건:

```java
@Bean
ApplicationRunner baseInitDataApplicationRunner() {
    return args -> self.makeSampleData();   // this 가 아닌 프록시(self)로 불러야 @Transactional 적용
}

@Transactional
public void makeSampleData() {
    if (postService.count() > 0) return;    // 이미 있으면 중복으로 넣지 않음
    Post post1 = postService.write("제목 1", "내용 1");
    ...
    post1.addComment("댓글 1-1");           // 1번 글: 댓글 3건, 2번 글: 2건, 3번 글: 0건
}
```

> 배울 점: `mappedBy` 는 "외래키는 반대편(댓글)에 있다"는 뜻입니다. cascade REMOVE 덕분에 글을 지우면 댓글도 지워집니다. `@Transactional` 은 프록시로 동작해서, 같은 클래스 안에서 `this.메서드()` 로 부르면 적용되지 않습니다.

### [003-1](https://github.com/zoek1211/rest-api-2/tree/003-1) 공통 응답·오류 처리와 글 작성 테스트 (실패)

**모든 API 의 응답 모양**:

```java
public record RsData<T>(
        String resultCode,              // "201-1"
        @JsonIgnore int statusCode,     // 201 (JSON 에는 안 나감)
        String msg,
        T data
) {
    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data); // 앞자리 → 상태코드
    }
}
```

**HTTP 상태코드를 RsData 에 맞추는 AOP** — 컨트롤러가 RsData 를 반환하면 자동으로:

```java
@Around("execution(public com.back.global.rsData.RsData *(..)) && ...")
public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();                    // 컨트롤러 실행
    response.setStatus(((RsData<?>) result).statusCode());  // 201 등으로 설정
    return result;
}
```

**예외 → 오류 응답**:

```java
@ExceptionHandler(NoSuchElementException.class)          // Optional.get() 에 값이 없을 때
public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
    return toResponseEntity(new RsData<>("404-1", "해당 데이터가 존재하지 않습니다."));
}
// MethodArgumentNotValidException → 400-1 "title-NotBlank-..."
// HttpMessageNotReadableException → 400-1 "요청 본문이 올바르지 않습니다."
```

**테스트를 먼저 쓴다 (빨강)**:

```java
@ActiveProfiles("test") @SpringBootTest @AutoConfigureMockMvc
@Transactional   // 테스트마다 롤백
public class ApiV1PostControllerTest {
    @Test
    void write() throws Exception {
        mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"title": "새 제목", "content": "새 내용"}"""))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.resultCode").value("201-1"));
    }
}
```

이 시점엔 API 가 없으므로 404 가 나서 **실패**합니다. 그게 정상입니다.

> 배울 점: TDD 는 "어떻게 동작해야 하는지"를 테스트로 먼저 적고, 실패하는 것을 확인한 다음 구현합니다.

### [003-2](https://github.com/zoek1211/rest-api-2/tree/003-2) 글 작성 구현

테스트를 통과시킬 만큼만 구현합니다 (초록).

```java
@RestController
@RequestMapping("/api/v1/posts")
public class ApiV1PostController {
    public record PostWriteReqBody(
            @NotBlank @Size(min = 2, max = 100)  String title,
            @NotBlank @Size(min = 2, max = 5000) String content
    ) { }

    @PostMapping
    @Transactional
    public RsData<PostDto> write(@RequestBody @Valid PostWriteReqBody reqBody) {
        Post post = postService.write(reqBody.title(), reqBody.content());
        return new RsData<>("201-1", "%d번 글이 작성되었습니다.".formatted(post.getId()), new PostDto(post));
    }
}
```

```java
// 엔티티 대신 DTO 로 응답: 양방향 관계의 무한 루프를 막고, 응답 형식을 엔티티와 분리
public record PostDto(int id, LocalDateTime createDate, LocalDateTime modifyDate,
                      String title, String content) {
    public PostDto(Post post) { this(post.getId(), post.getCreateDate(), ...); }
}
```

요청과 응답 예:

```http
POST /api/v1/posts
{"title": "curl 제목", "content": "curl 내용"}

201
{"resultCode":"201-1","msg":"4번 글이 작성되었습니다.","data":{"id":4,"title":"curl 제목", ...}}
```

> 배울 점: `@Valid` 가 실패하면 컨트롤러 메서드는 실행되지 않고 바로 400 이 됩니다.

### [004-1](https://github.com/zoek1211/rest-api-2/tree/004-1) 글 삭제 테스트 (실패)

샘플 데이터를 "주어진 상황(given)"으로 씁니다. 1번 글에는 댓글이 3건 있습니다.

```java
@Test
void deletePost() throws Exception {
    assertThat(countCommentsOf(1)).isEqualTo(3);                 // given

    mvc.perform(delete("/api/v1/posts/1"))                        // when
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.msg").value("1번 글이 삭제되었습니다."));

    assertThat(postRepository.findById(1)).isEmpty();            // then: 글 삭제
    assertThat(countCommentsOf(1)).isZero();                      // then: 댓글도 삭제
}

// 댓글 리포지터리가 없으므로 JPQL 로 직접 셈 (쿼리 전에 자동 flush)
private long countCommentsOf(int postId) {
    return em.createQuery("select count(c) from PostComment c where c.post.id = :postId", Long.class)
             .setParameter("postId", postId).getSingleResult();
}
```

없는 글(`DELETE /api/v1/posts/999`)은 404 `404-1` 이어야 합니다. 아직 API 가 없어 두 테스트 모두 **실패**합니다.

> 배울 점: 테스트 메서드 이름을 `delete` 로 지으면 static import 한 `delete(...)` 와 겹쳐 컴파일 오류가 납니다.

### [004-2](https://github.com/zoek1211/rest-api-2/tree/004-2) 글 삭제 구현

글을 찾아서(없으면 404) 지웁니다. 댓글은 cascade REMOVE 로 함께 지워집니다 (초록).

```java
// ApiV1PostController
@DeleteMapping("/{id}")                      // HTTP DELETE + 주소 끝 숫자를 {id} 로
@Transactional
public RsData<Void> delete(@PathVariable int id) {   // /api/v1/posts/1 → id = 1
    Post post = postService.findById(id).get();      // 없으면 NoSuchElementException → 404
    postService.delete(post);                        // cascade 가 먼저 전파 → 댓글, 글 순서로 DELETE
    return new RsData<>("200-1", "%d번 글이 삭제되었습니다.".formatted(id));
}
```

```java
// PostService
public Optional<Post> findById(int id) { return postRepository.findById(id); }
public void delete(Post post) { postRepository.delete(post); }
```

요청과 응답 예 (실제 앱에서 확인 — 삭제 후 DB 에는 글 2·3, 댓글 4·5 만 남음):

```http
DELETE /api/v1/posts/1
200
{"resultCode":"200-1","msg":"1번 글이 삭제되었습니다.","data":null}

DELETE /api/v1/posts/999
404
{"resultCode":"404-1","msg":"해당 데이터가 존재하지 않습니다.","data":null}
```

> 배울 점: 같은 주소라도 HTTP 메서드(GET/POST/DELETE)로 하는 일을 나누는 것이 REST 방식이고, `Optional.get()` 의 예외를 전역 예외 처리가 404 로 바꿔 줍니다.

### [005-1](https://github.com/zoek1211/rest-api-2/tree/005-1) 댓글 작성 테스트 (실패)

댓글은 글에 속하므로 주소도 글 아래에 둡니다: `POST /api/v1/posts/{postId}/comments`. 새 테스트 클래스를 만들고 테스트부터 씁니다.

```java
@Test
void write() throws Exception {
    assertThat(countCommentsOf(1)).isEqualTo(3);                       // given: 1번 글 댓글 3건

    ResultActions resultActions = mvc.perform(
            post("/api/v1/posts/1/comments")                            // when
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"content\": \"새 댓글\"}"));

    assertThat(countCommentsOf(1)).isEqualTo(4);                       // then: 4건
    PostComment comment = findLatestCommentOf(1);                       // id 가 가장 큰 댓글
    resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").value(comment.getId()))   // 응답 id == 실제 저장된 id
            .andExpect(jsonPath("$.data.postId").value(1));
}

// 최신 댓글: id 내림차순으로 정렬해서 맨 위 1건
private PostComment findLatestCommentOf(int postId) {
    return em.createQuery("select c from PostComment c where c.post.id = :postId order by c.id desc", PostComment.class)
             .setParameter("postId", postId).setMaxResults(1).getSingleResult();
}
```

없는 글(`POST /api/v1/posts/999/comments`)은 404 `404-1` 이어야 합니다. 아직 API 가 없어 두 테스트 모두 **실패**합니다 (댓글 수가 3 그대로, 404 응답이 RsData 형식이 아님).

> 배울 점: 주소를 `/posts/{postId}/comments` 처럼 중첩하면 "어느 글의 댓글인지"가 주소에 드러납니다.
