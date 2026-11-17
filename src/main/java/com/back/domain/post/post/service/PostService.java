package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 글과 댓글에 대한 비즈니스 로직을 담당하는 서비스입니다.
 *
 * <p>컨트롤러(요청/응답 처리)와 리포지터리(DB 접근) 사이에 있는 계층입니다.
 * 컨트롤러는 리포지터리를 직접 쓰지 않고 서비스를 통해서 일을 시킵니다.
 *
 * <ul>
 *   <li>{@code @Service}: 스프링이 이 클래스를 빈으로 등록 (컴포넌트 스캔 대상)</li>
 *   <li>{@code @RequiredArgsConstructor}: 롬복이 final 필드를 받는 생성자를 만들어 줌.
 *       스프링은 생성자가 하나면 그 생성자로 의존성(PostRepository)을 주입합니다 (생성자 주입).</li>
 * </ul>
 *
 * <p>지금은 개수 세기, 글 작성, 글 찾기, 글 삭제, 댓글 작성이 있습니다.
 * 다음 단계(006)에서 댓글 삭제를 이 서비스에 추가합니다.
 * 댓글은 글에 속해 있어 별도 서비스를 두지 않습니다.
 */
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    /** 저장된 글의 개수. 샘플 데이터를 이미 넣었는지 확인할 때 씁니다. */
    public long count() {
        return postRepository.count();
    }

    /**
     * 새 글을 작성(저장)합니다.
     *
     * @return 저장된 글. {@code save} 가 INSERT 를 실행하므로 id 가 채워져 있습니다.
     */
    public Post write(String title, String content) {
        Post post = new Post(title, content);

        return postRepository.save(post);
    }

    /**
     * id 로 글을 찾습니다.
     *
     * <p>{@code Optional} 은 "값이 있을 수도, 없을 수도 있음"을 나타내는 상자입니다.
     * null 을 그냥 돌려주면 받는 쪽이 null 검사를 잊기 쉬운데, Optional 은 꺼낼 때 없음을 처리하도록 유도합니다.
     * 컨트롤러에서 {@code .get()} 으로 꺼낼 때 글이 없으면 NoSuchElementException 이 나고,
     * GlobalExceptionHandler 가 404 로 바꿔 응답합니다.
     *
     * @param id 글 번호
     * @return 찾은 글, 없으면 빈 Optional
     */
    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    /**
     * 글을 삭제합니다.
     *
     * <p>Post.comments 에 cascade REMOVE 가 있어서, 글을 지우면 그 글의 댓글들도 함께 DELETE 됩니다.
     * cascade 는 글의 삭제를 예약하기 전에 자식(댓글)에게 먼저 전파되므로 댓글 DELETE 가 먼저 실행되고,
     * 그 덕분에 "댓글이 글을 가리키는 외래키" 위반 없이 글까지 지울 수 있습니다.
     *
     * @param post 삭제할 글 (findById 로 찾은 영속 상태의 글)
     */
    public void delete(Post post) {
        postRepository.delete(post);
    }

    /**
     * 글에 댓글을 작성합니다.
     *
     * <p>댓글은 글을 통해서만 만듭니다({@code Post.addComment}). 댓글 리포지터리에 save 하지 않아도,
     * 트랜잭션 안의 영속 상태 글에 추가했으므로 flush 때 cascade PERSIST 로 INSERT 됩니다.
     *
     * @param post    댓글을 달 글 (findById 로 찾은 영속 상태의 글)
     * @param content 댓글 내용
     * @return 새 댓글. 아직 flush 전이면 id 가 0 이므로, id 가 필요하면 {@link #flush()} 를 먼저 부릅니다.
     */
    public PostComment writeComment(Post post, String content) {
        return post.addComment(content);
    }

    /**
     * 지금까지 쌓인 변경을 DB 에 바로 반영(flush)합니다. 커밋은 아닙니다.
     *
     * <p>새 댓글의 id 는 INSERT 가 실행되어야 정해집니다(IDENTITY 전략).
     * 응답에 새 댓글 번호를 넣으려면 트랜잭션이 끝나기 전에 INSERT 를 실행시켜야 하므로 flush 합니다.
     */
    public void flush() {
        postRepository.flush();
    }
}
