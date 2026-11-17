package com.back.domain.post.postComment.entity;

import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 댓글(PostComment) 엔티티. 하나의 글에 달린 짧은 글입니다. (용어는 GLOSSARY.md 참고)
 *
 * <p>DB 의 post_comment 테이블과 연결됩니다.
 * 댓글은 글 없이 존재할 수 없으므로, 직접 만들지 않고 {@code Post.addComment()} 로 만듭니다.
 */
@Entity
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {
    /**
     * 이 댓글이 달린 글.
     *
     * <ul>
     *   <li>{@code @ManyToOne}: 댓글 여러 개가 글 1개에 속함 (N:1 관계).
     *       post_comment 테이블에 post_id 외래키 컬럼이 생깁니다. 이 필드가 관계의 주인입니다.</li>
     *   <li>{@code fetch = LAZY}(지연 로딩): 댓글을 조회할 때 글을 바로 같이 조회하지 않고,
     *       {@code getPost()} 로 실제로 글의 내용을 쓸 때 조회합니다. 필요 없는 SQL 을 줄이기 위해서입니다.</li>
     * </ul>
     */
    @ManyToOne(fetch = LAZY)
    private Post post;

    /** 댓글 내용 */
    private String content;

    /**
     * 댓글을 만드는 생성자. 보통은 {@code Post.addComment()} 안에서만 호출합니다.
     *
     * @param post    댓글이 달릴 글
     * @param content 댓글 내용
     */
    public PostComment(Post post, String content) {
        this.post = post;
        this.content = content;
    }
}
