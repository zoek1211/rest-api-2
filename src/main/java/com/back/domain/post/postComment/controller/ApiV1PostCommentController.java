package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.PostCommentDto;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 댓글 API 컨트롤러입니다. 주소는 /api/v1/posts/{postId}/comments 로 시작합니다.
 *
 * <p>댓글은 글에 속하므로 주소도 글 아래에 중첩합니다. 주소만 봐도 "몇 번 글의 댓글"인지 알 수 있습니다.
 * {@code @RequestMapping} 의 {postId} 는 이 클래스의 모든 메서드에서 {@code @PathVariable int postId} 로 받을 수 있습니다.
 *
 * <p>댓글 전용 서비스가 없으므로 PostService 를 씁니다. (댓글은 글을 통해서만 다룸)
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;

    /**
     * 댓글 작성 요청 본문(JSON)을 받는 DTO. {@code {"content": "..."}}
     * 댓글 내용은 2~100자, 공백만은 안 됨.
     */
    public record PostCommentWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    /**
     * 댓글 작성. {@code POST /api/v1/posts/{postId}/comments}
     *
     * <ol>
     *   <li>글을 찾음 (없으면 NoSuchElementException → 404)</li>
     *   <li>글에 댓글을 추가</li>
     *   <li>flush 로 INSERT 를 바로 실행해서 새 댓글의 id 를 받음 (안 하면 응답의 id 가 0)</li>
     * </ol>
     *
     * @param postId  주소의 글 번호
     * @param reqBody 댓글 내용 (@Valid 로 검증, 실패하면 400)
     * @return 201-1 과 작성된 댓글
     */
    @PostMapping
    @Transactional
    public RsData<PostCommentDto> write(
            @PathVariable int postId,
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        Post post = postService.findById(postId).get();

        PostComment postComment = postService.writeComment(post, reqBody.content());

        postService.flush();

        return new RsData<>(
                "201-1",
                "%d번 댓글이 작성되었습니다.".formatted(postComment.getId()),
                new PostCommentDto(postComment)
        );
    }
}
