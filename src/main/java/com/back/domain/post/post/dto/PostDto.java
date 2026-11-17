package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;

import java.time.LocalDateTime;

/**
 * 글을 API 응답으로 내보낼 때 쓰는 DTO(Data Transfer Object)입니다.
 *
 * <p>왜 엔티티(Post)를 그대로 응답하지 않을까요?
 * <ul>
 *   <li>엔티티를 그대로 JSON 으로 바꾸면 comments → post → comments ... 처럼 서로를 계속 참조해서
 *       무한 루프가 생길 수 있습니다. (양방향 관계)</li>
 *   <li>엔티티 구조가 바뀌면 API 응답 형식도 같이 바뀌어 버립니다. DTO 로 분리하면 응답 형식을 따로 지킬 수 있습니다.</li>
 *   <li>밖으로 보여줄 필드만 고를 수 있습니다.</li>
 * </ul>
 *
 * @param id         글 번호
 * @param createDate 작성일
 * @param modifyDate 수정일
 * @param title      제목
 * @param content    내용
 */
public record PostDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String title,
        String content
) {
    /**
     * 엔티티로부터 DTO 를 만드는 생성자.
     * record 의 기본 생성자(모든 필드를 받는 생성자)를 {@code this(...)} 로 호출합니다.
     */
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getTitle(),
                post.getContent()
        );
    }
}
