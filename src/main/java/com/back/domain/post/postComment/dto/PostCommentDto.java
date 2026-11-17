package com.back.domain.post.postComment.dto;

import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;

/**
 * 댓글을 API 응답으로 내보낼 때 쓰는 DTO 입니다. (PostDto 와 같은 이유로 엔티티 대신 사용)
 *
 * <p>댓글 엔티티는 글(Post) 객체 전체를 들고 있지만, 응답에는 어느 글의 댓글인지 알 수 있도록
 * 글 번호(postId)만 넣습니다. 글 전체를 넣으면 글 → 댓글 목록 → 글 ... 로 이어지는 순환이 생길 수 있습니다.
 *
 * @param id         댓글 번호
 * @param createDate 작성일
 * @param modifyDate 수정일
 * @param postId     이 댓글이 달린 글 번호
 * @param content    댓글 내용
 */
public record PostCommentDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        int postId,
        String content
) {
    /**
     * 엔티티로부터 DTO 를 만드는 생성자.
     *
     * <p>{@code postComment.getPost().getId()}: 댓글의 post 는 지연 로딩(LAZY) 프록시일 수 있지만,
     * 하이버네이트 프록시는 id 를 이미 알고 있어서 getId() 만으로는 글을 조회하는 SQL 이 나가지 않습니다.
     */
    public PostCommentDto(PostComment postComment) {
        this(
                postComment.getId(),
                postComment.getCreateDate(),
                postComment.getModifyDate(),
                postComment.getPost().getId(),
                postComment.getContent()
        );
    }
}
