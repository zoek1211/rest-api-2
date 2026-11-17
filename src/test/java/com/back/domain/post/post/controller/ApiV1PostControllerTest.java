package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 글 API(/api/v1/posts) 테스트입니다.
 *
 * <p>테스트 지점은 HTTP API 하나입니다. 실제 서버를 띄우지 않고 MockMvc 로 HTTP 요청을 흉내 내고,
 * 응답(상태코드, resultCode, msg, data)과 요청 후 DB 에 남은 결과를 확인합니다.
 *
 * <ul>
 *   <li>{@code @ActiveProfiles("test")}: test 프로필(메모리 H2 DB). 샘플 데이터(글 3건, 댓글 5건)도 들어 있음</li>
 *   <li>{@code @SpringBootTest}: 실제 앱과 같은 스프링 컨테이너 전체를 띄움</li>
 *   <li>{@code @AutoConfigureMockMvc}: MockMvc 를 자동으로 만들어 주입받을 수 있게 함</li>
 *   <li>{@code @Transactional}: 테스트 메서드마다 트랜잭션을 열고, 끝나면 롤백.
 *       그래서 한 테스트에서 만든 글이 다른 테스트에 영향을 주지 않음</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostControllerTest {
    /** HTTP 요청을 흉내 내는 도구 */
    @Autowired
    private MockMvc mvc;

    /** 요청 후 DB 에 실제로 저장/삭제되었는지 확인하는 용도 */
    @Autowired
    private PostRepository postRepository;

    /**
     * JPA 의 EntityManager. 댓글 전용 리포지터리가 없어서, 댓글이 DB 에 남아 있는지
     * JPQL 쿼리로 직접 세어 볼 때 씁니다.
     */
    @Autowired
    private EntityManager em;

    /**
     * 특정 글에 달린 댓글 수를 DB 에서 셉니다.
     *
     * <p>JPQL 은 테이블이 아니라 엔티티(PostComment)를 대상으로 쓰는 SQL 비슷한 쿼리입니다.
     * 쿼리를 실행하기 전에 JPA 가 아직 DB 에 반영 안 된 변경(예: 삭제)을 먼저 flush 하므로,
     * 같은 트랜잭션 안에서 방금 한 삭제도 결과에 반영됩니다.
     * (MockMvc 요청은 테스트와 같은 스레드에서 실행되어, 컨트롤러의 @Transactional 이 테스트의 트랜잭션에
     * 합류합니다. 그래서 요청이 바꾼 내용을 여기서 볼 수 있고, 테스트가 끝나면 함께 롤백됩니다)
     */
    private long countCommentsOf(int postId) {
        return em.createQuery(
                        "select count(c) from PostComment c where c.post.id = :postId", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }

    @Test
    @DisplayName("글 작성 - 제목과 내용을 보내면 201 과 함께 새 글이 저장된다")
    void write() throws Exception {
        // when: 제목·내용을 JSON 으로 보내 글 작성 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON) // 본문이 JSON 이라고 알림
                                .content("""
                                        {
                                            "title": "새 제목",
                                            "content": "새 내용"
                                        }
                                        """) // 자바 텍스트 블록(""" """): 여러 줄 문자열
                )
                .andDo(print()); // 요청/응답 내용을 콘솔에 출력 (디버깅용)

        // 방금 저장된 글 = DB 에서 id 가 가장 큰 글
        // (id 는 자동 증가라 다른 테스트 실행 순서에 따라 4가 아닐 수도 있으므로 숫자를 고정하지 않음)
        Post post = postRepository.findAll()
                .stream()
                .max(Comparator.comparingInt(Post::getId))
                .get();

        // then: DB 에 보낸 그대로 저장되었는지
        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");

        // then: 응답이 스펙대로인지
        resultActions
                .andExpect(status().isCreated()) // 201
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.statusCode").doesNotExist()) // statusCode 는 JSON 에 나가지 않음
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.title").value("새 제목"))
                .andExpect(jsonPath("$.data.content").value("새 내용"));
    }

    @Test
    @DisplayName("글 작성 - 제목 없이 보내면 400 이고 글이 저장되지 않는다")
    void writeWithoutTitle() throws Exception {
        long countBefore = postRepository.count();

        // when: title 필드 없이 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 내용"
                                        }
                                        """)
                )
                .andDo(print());

        // then: 400, 어떤 필드가 어떤 규칙을 어겼는지 메시지에 나옴
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                // 메시지 형식: 필드명-규칙명-기본메시지. 기본메시지는 언어 설정에 따라 다르므로 앞부분만 확인
                .andExpect(jsonPath("$.msg").value(startsWith("title-NotBlank-")));

        // then: 글 개수가 그대로 (저장되지 않음)
        assertThat(postRepository.count()).isEqualTo(countBefore);
    }

    @Test
    @DisplayName("글 삭제 - 있는 글을 지우면 200 이고, 글과 그 글의 댓글이 모두 사라진다")
    // 메서드 이름을 delete 로 지으면, 자바에서는 클래스 안의 메서드가 같은 이름의 static import
    // (MockMvcRequestBuilders.delete) 를 인자 개수와 상관없이 가립니다.
    // 그러면 delete("/api/v1/posts/1") 이 이 테스트 메서드를 가리키게 되어 컴파일 오류가 나므로 deletePost 로 지음
    void deletePost() throws Exception {
        // given: 샘플 데이터의 1번 글에는 댓글이 3건 (댓글 1-1, 1-2, 1-3)
        assertThat(countCommentsOf(1)).isEqualTo(3);

        // when: 1번 글 삭제 요청 (본문 없음)
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1") // 주소의 1 이 컨트롤러의 {id} 로 들어감
                )
                .andDo(print());

        // then: 응답이 스펙대로인지
        resultActions
                .andExpect(status().isOk()) // 200
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 글이 삭제되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist()); // 삭제는 돌려줄 데이터가 없음 (null)

        // then: 글이 DB 에서 사라짐
        assertThat(postRepository.findById(1)).isEmpty();

        // then: 그 글의 댓글도 함께 사라짐 (Post.comments 의 cascade REMOVE)
        assertThat(countCommentsOf(1)).isZero();
    }

    @Test
    @DisplayName("글 삭제 - 없는 글을 지우려 하면 404 이고 아무것도 지워지지 않는다")
    void deleteNotExistingPost() throws Exception {
        long countBefore = postRepository.count();

        // when: 존재하지 않는 999번 글 삭제 요청
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/999")
                )
                .andDo(print());

        // then: 404, RsData 형식의 오류 응답
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));

        // then: 글 개수가 그대로
        assertThat(postRepository.count()).isEqualTo(countBefore);
    }
}
