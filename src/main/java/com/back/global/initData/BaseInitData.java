package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * 샘플 데이터(글 3건, 댓글 5건)를 앱이 시작될 때 자동으로 넣는 설정입니다. (용어는 GLOSSARY.md 참고)
 *
 * <p>dev 와 test 프로필 모두에서 실행됩니다. 그래서 테스트도 이 샘플 데이터를 기준으로 작성합니다.
 */
@Configuration // 스프링 설정 클래스. 이 안의 @Bean 메서드가 반환하는 객체를 빈으로 등록
@RequiredArgsConstructor
public class BaseInitData {
    /**
     * 자기 자신(BaseInitData)을 주입받습니다. 왜 필요할까요?
     *
     * <p>{@code @Transactional} 은 스프링이 이 클래스를 감싼 "프록시" 객체를 통해 동작합니다.
     * 그런데 클래스 안에서 {@code this.work1()} 처럼 직접 부르면 프록시를 거치지 않아서
     * 트랜잭션이 적용되지 않습니다. 그래서 프록시인 {@code self} 를 통해 {@code self.makeSampleData()} 로 부릅니다.
     *
     * <p>{@code @Lazy}: 자기 자신을 주입받으면 "만들어지기 전에 자기가 필요"한 순환 참조가 됩니다.
     * {@code @Lazy} 를 붙이면 실제로 쓰는 시점에 가져오므로 순환 참조 문제가 생기지 않습니다.
     */
    @Autowired
    @Lazy
    private BaseInitData self;

    private final PostService postService;

    /**
     * {@code ApplicationRunner} 빈은 스프링 부트가 앱 시작을 마친 직후 자동으로 실행합니다.
     * 여기서 샘플 데이터를 넣는 {@code makeSampleData()} 를 부릅니다.
     */
    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.makeSampleData();
        };
    }

    /**
     * 샘플 데이터를 넣습니다. (참고 강의에서는 이 메서드 이름이 work1 입니다)
     *
     * <p>{@code @Transactional}: 메서드 전체가 하나의 트랜잭션으로 실행됩니다.
     * 글은 {@code save} 할 때 바로 INSERT 됩니다(id 를 IDENTITY 로 받아야 하므로).
     * 댓글은 {@code addComment()} 로 리스트에 넣기만 하고, 커밋 직전 flush 할 때
     * Post.comments 의 cascade PERSIST 가 새 댓글들을 저장해서 INSERT 됩니다.
     */
    @Transactional
    public void makeSampleData() {
        // 글이 하나라도 있으면 이미 샘플 데이터를 넣은 것이므로 아무것도 하지 않음
        // (dev 의 파일 DB 는 재시작해도 데이터가 남아 있으므로, 이 검사가 없으면 실행할 때마다 중복으로 쌓임)
        if (postService.count() > 0) return;

        Post post1 = postService.write("제목 1", "내용 1");
        Post post2 = postService.write("제목 2", "내용 2");
        postService.write("제목 3", "내용 3"); // 3번 글에는 댓글을 달지 않음 (댓글 없는 글 시험용)

        // 1번 글에 댓글 3건 → 댓글 id 1, 2, 3
        post1.addComment("댓글 1-1");
        post1.addComment("댓글 1-2");
        post1.addComment("댓글 1-3");

        // 2번 글에 댓글 2건 → 댓글 id 4, 5
        post2.addComment("댓글 2-1");
        post2.addComment("댓글 2-2");
    }
}
