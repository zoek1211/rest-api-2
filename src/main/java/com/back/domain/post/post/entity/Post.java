package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

/**
 * 글(Post) 엔티티. 제목과 내용으로 이루어진 글 한 편입니다. (용어는 GLOSSARY.md 참고)
 *
 * <p>{@code @Entity} 가 붙은 클래스는 JPA 가 관리하며 DB 의 post 테이블과 연결됩니다.
 * id, createDate, modifyDate 는 부모인 BaseEntity 에서 물려받습니다.
 *
 * <p>댓글은 글에 속해 있으므로, 댓글을 추가하거나 지우는 일은 글을 통해서 합니다.
 * (댓글 전용 리포지터리가 없음. 글을 저장하면 댓글도 함께 저장됨)
 */
@Entity
@Getter
// JPA 는 DB 에서 데이터를 읽어 객체를 만들 때 "인자 없는 생성자"가 필요합니다.
// 롬복의 @NoArgsConstructor 가 그 생성자를 만들어 줍니다.
@NoArgsConstructor
public class Post extends BaseEntity {
    /** 글 제목 */
    private String title;

    /** 글 내용 */
    private String content;

    /**
     * 이 글에 달린 댓글 목록.
     *
     * <ul>
     *   <li>{@code @OneToMany}: 글 1개에 댓글 여러 개 (1:N 관계)</li>
     *   <li>{@code mappedBy = "post"}: 관계의 주인은 PostComment.post 필드라는 뜻.
     *       실제 외래키(post_comment.post_id)는 댓글 테이블에 있고, 이 리스트는 읽기용 "거울" 입니다.</li>
     *   <li>{@code cascade = PERSIST}: 글을 저장하면 리스트에 새로 추가된 댓글도 함께 INSERT</li>
     *   <li>{@code cascade = REMOVE}: 글을 삭제하면 댓글도 함께 DELETE (글 없는 댓글은 존재할 수 없으므로)</li>
     *   <li>{@code orphanRemoval = true}: 리스트에서 빠진 댓글(고아)은 DB 에서도 DELETE</li>
     * </ul>
     *
     * <p>바로 {@code new ArrayList<>()} 로 초기화해서 새 글에서도 null 이 아니게 합니다.
     * {@code final} 은 "우리 코드에서 이 필드에 다른 리스트를 대입하지 않는다" 는 표시입니다.
     * (하이버네이트는 내부적으로 리플렉션을 써서 이 필드를 자기 컬렉션 타입(PersistentBag)으로 바꿔 넣습니다.
     * 그래서 엄밀히는 final 이어도 값이 바뀔 수 있고, JPA 명세는 영속 필드에 final 을 권장하지 않습니다.
     * 참고 강의를 따라 final 을 유지합니다.)
     */
    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<PostComment> comments = new ArrayList<>();

    /** 새 글을 만들 때 쓰는 생성자. id 와 날짜는 저장될 때 자동으로 정해집니다. */
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 이 글에 댓글을 추가합니다.
     *
     * <p>댓글 객체를 만들고 리스트에 넣기만 합니다. 이 글이 이미 저장된(영속 상태) 글이라면,
     * 트랜잭션이 끝나기 전 flush 할 때 cascade PERSIST 가 리스트의 새 댓글까지 저장해서 INSERT 가 실행됩니다.
     *
     * @param content 댓글 내용
     * @return 새로 만든 댓글 (아직 INSERT 전이면 id 는 0)
     */
    public PostComment addComment(String content) {
        PostComment postComment = new PostComment(this, content);
        comments.add(postComment);

        return postComment;
    }
}
