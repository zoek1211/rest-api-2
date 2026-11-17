package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 글 API 컨트롤러입니다. 주소는 /api/v1/posts 로 시작합니다.
 *
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}.
 *       메서드가 반환한 객체를 HTML 화면이 아니라 JSON 으로 바꿔 응답 본문에 씁니다.</li>
 *   <li>{@code @RequestMapping("/api/v1/posts")}: 이 클래스의 모든 메서드 주소 앞에 붙는 공통 경로.
 *       "api" 는 화면이 아닌 API 라는 뜻, "v1" 은 API 버전(나중에 형식이 바뀌면 v2 를 따로 만들 수 있게)</li>
 *   <li>클래스 이름 ApiV1PostController 도 주소와 맞춰서 지었습니다.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;

    /**
     * 글 작성 요청 본문(JSON)을 받는 DTO.
     *
     * <p>{@code {"title": "...", "content": "..."}} 형태의 JSON 이 이 record 로 바뀝니다.
     * 필드에 붙인 검증 어노테이션은 {@code @Valid} 가 있을 때 검사됩니다.
     * <ul>
     *   <li>{@code @NotBlank}: null, 빈 문자열(""), 공백만(" ") 모두 안 됨</li>
     *   <li>{@code @Size(min, max)}: 글자 수 범위</li>
     * </ul>
     * 컨트롤러 안에 둔 이유: 이 요청에서만 쓰는 모양이라서. (참고 강의 방식)
     */
    public record PostWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,
            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    /**
     * 글 작성. {@code POST /api/v1/posts}
     *
     * <ul>
     *   <li>{@code @RequestBody}: 요청 본문 JSON 을 PostWriteReqBody 객체로 바꿔서 받음</li>
     *   <li>{@code @Valid}: 받은 값을 검증. 실패하면 이 메서드는 실행되지 않고
     *       MethodArgumentNotValidException 이 발생 → GlobalExceptionHandler 가 400 응답</li>
     *   <li>{@code @Transactional}: 메서드 전체를 하나의 트랜잭션으로. 끝나면 커밋</li>
     * </ul>
     *
     * @return 201-1 과 작성된 글. HTTP 상태 201 은 ResponseAspect 가 resultCode 를 보고 설정
     */
    @PostMapping
    @Transactional
    public RsData<PostDto> write(
            @RequestBody @Valid PostWriteReqBody reqBody
    ) {
        Post post = postService.write(reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }
}
