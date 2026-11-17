package com.back.domain.post.postComment.controller;

import com.back.domain.post.postComment.entity.PostComment;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 댓글 API(/api/v1/posts/{postId}/comments) 테스트입니다.
 *
 * <p>글 API 테스트(ApiV1PostControllerTest)와 같은 방식입니다.
 * MockMvc 로 HTTP 요청을 흉내 내고, 응답과 요청 후 DB 에 남은 댓글을 확인합니다.
 * test 프로필의 샘플 데이터: 1번 글에 댓글 1~3, 2번 글에 댓글 4~5, 3번 글에는 댓글 없음.
 *
 * <p>{@code @Transactional}: 테스트마다 롤백되므로 한 테스트에서 단 댓글이 다른 테스트에 남지 않습니다.
 * MockMvc 요청은 테스트와 같은 스레드에서 실행되어 컨트롤러의 트랜잭션이 테스트의 트랜잭션에 합류합니다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentControllerTest {
    /** HTTP 요청을 흉내 내는 도구 */
    @Autowired
    private MockMvc mvc;

    /**
     * 댓글 전용 리포지터리가 없으므로, 댓글이 DB 에 어떻게 남았는지 JPQL 로 직접 확인할 때 씁니다.
     * (JPQL 쿼리 전에 아직 반영 안 된 변경이 자동으로 flush 됨)
     */
    @Autowired
    private EntityManager em;

    /** 특정 글에 달린 댓글 수를 DB 에서 셉니다. */
    private long countCommentsOf(int postId) {
        return em.createQuery(
                        "select count(c) from PostComment c where c.post.id = :postId", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }

    /** 해당 id 의 댓글이 DB 에 남아 있는지 확인합니다. */
    private boolean commentExists(int commentId) {
        return em.createQuery(
                        "select count(c) from PostComment c where c.id = :id", Long.class)
                .setParameter("id", commentId)
                .getSingleResult() > 0;
    }

    /** 특정 글에 가장 최근에 달린 댓글(id 가 가장 큰 댓글)을 DB 에서 가져옵니다. */
    private PostComment findLatestCommentOf(int postId) {
        return em.createQuery(
                        "select c from PostComment c where c.post.id = :postId order by c.id desc", PostComment.class)
                .setParameter("postId", postId)
                .setMaxResults(1) // 맨 위 1건만
                .getSingleResult();
    }

    @Test
    @DisplayName("댓글 작성 - 1번 글에 내용을 보내면 201 과 함께 새 댓글이 저장된다")
    void write() throws Exception {
        // given: 샘플 데이터의 1번 글에는 댓글이 3건
        assertThat(countCommentsOf(1)).isEqualTo(3);

        // when: 1번 글에 댓글 작성 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/1/comments") // 주소의 1 이 컨트롤러의 {postId} 로 들어감
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print());

        // then: 1번 글의 댓글이 4건이 되고, 가장 최근 댓글이 방금 보낸 내용
        assertThat(countCommentsOf(1)).isEqualTo(4);
        PostComment postComment = findLatestCommentOf(1);
        assertThat(postComment.getContent()).isEqualTo("새 댓글");

        // then: 응답이 스펙대로인지. 응답의 id 는 실제로 저장된 댓글의 id 여야 함 (0 이면 안 됨)
        resultActions
                .andExpect(status().isCreated()) // 201
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 작성되었습니다.".formatted(postComment.getId())))
                .andExpect(jsonPath("$.data.id").value(postComment.getId()))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.content").value("새 댓글"));
    }

    @Test
    @DisplayName("댓글 작성 - 없는 글에 댓글을 달려고 하면 404 이다")
    void writeOnNotExistingPost() throws Exception {
        // when: 존재하지 않는 999번 글에 댓글 작성 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/999/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print());

        // then: 404, RsData 형식의 오류 응답
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 1번 글의 1번 댓글을 지우면 200 이고 그 댓글만 사라진다")
    // 메서드 이름을 delete 로 지으면 static import 한 delete(...) 를 가리므로 deleteComment 로 지음
    void deleteComment() throws Exception {
        // given: 1번 글에 댓글 1, 2, 3 이 있음
        assertThat(countCommentsOf(1)).isEqualTo(3);
        assertThat(commentExists(1)).isTrue();

        // when: 1번 글의 1번 댓글 삭제 요청 (주소에 글 번호와 댓글 번호가 모두 들어감)
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1/comments/1")
                )
                .andDo(print());

        // then: 응답이 스펙대로인지
        resultActions
                .andExpect(status().isOk()) // 200
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 삭제되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist()); // 삭제는 돌려줄 데이터가 없음 (null)

        // then: 1번 댓글만 사라지고 1번 글의 나머지 댓글(2, 3)은 남음
        assertThat(commentExists(1)).isFalse();
        assertThat(countCommentsOf(1)).isEqualTo(2);
    }

    @Test
    @DisplayName("댓글 삭제 - 다른 글의 댓글을 이 글 주소로 지우려 하면 404 이고 그 댓글은 남는다")
    void deleteCommentOfOtherPost() throws Exception {
        // given: 4번 댓글은 2번 글의 댓글 (1번 글의 댓글이 아님)
        assertThat(commentExists(4)).isTrue();

        // when: 1번 글 주소로 4번 댓글 삭제 요청
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1/comments/4")
                )
                .andDo(print());

        // then: 1번 글에는 4번 댓글이 없으므로 404
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));

        // then: 엉뚱한 댓글이 지워지지 않음
        assertThat(commentExists(4)).isTrue();
    }
}
