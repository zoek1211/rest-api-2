package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 글(Post)을 DB 에 저장하고 조회하는 리포지터리입니다.
 *
 * <p>인터페이스만 선언하면 Spring Data JPA 가 실행 시점에 구현체를 자동으로 만들어 줍니다.
 * {@code JpaRepository<Post, Integer>} 의 뜻: 다루는 엔티티는 Post, 기본키(id) 타입은 Integer.
 *
 * <p>상속만으로 save, findById, findAll, count, delete 같은 메서드를 바로 쓸 수 있습니다.
 *
 * <p>댓글 리포지터리는 따로 만들지 않습니다. 댓글은 글을 통해서만 저장·삭제하기 때문입니다.
 */
public interface PostRepository extends JpaRepository<Post, Integer> {
}
