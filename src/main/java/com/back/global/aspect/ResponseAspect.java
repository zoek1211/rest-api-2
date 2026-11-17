package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 컨트롤러가 RsData 를 반환하면, HTTP 응답 상태코드를 RsData 의 statusCode 로 맞춰 주는 AOP 입니다.
 *
 * <p>왜 필요할까요? 컨트롤러 메서드가 그냥 RsData 를 반환하면 스프링은 기본으로 200 OK 를 보냅니다.
 * 글 작성 성공(201-1)이면 HTTP 상태도 201 이어야 하는데, 매 메서드마다 상태코드를 설정하는 코드를
 * 반복해서 쓰는 대신 이 클래스 한 곳에서 처리합니다.
 *
 * <p>AOP(관점 지향 프로그래밍): 여러 메서드에 공통으로 끼워 넣을 동작을 따로 모아두는 방법입니다.
 * <ul>
 *   <li>{@code @Aspect}: 이 클래스가 AOP 설정(어디에, 무엇을 끼워 넣을지)임을 표시</li>
 *   <li>{@code @Component}: 빈으로 등록해야 스프링이 이 Aspect 를 적용함</li>
 * </ul>
 * (AOP 에 필요한 aspectjweaver 라이브러리는 따로 추가하지 않았습니다.
 * data-jpa 스타터 → spring-data-jpa → spring-aspects → aspectjweaver 로 이어지는 전이 의존성으로 들어옵니다.
 * 그래서 JPA 를 빼면 이 Aspect 가 조용히 동작하지 않게 되니 주의하세요.)
 */
@Aspect
@Component
public class ResponseAspect {
    /**
     * 현재 요청의 HTTP 응답 객체.
     * 이 Aspect 는 싱글톤 빈이지만, 스프링이 "현재 요청의 응답"으로 연결해 주는 프록시를 주입하므로
     * 요청마다 올바른 응답 객체를 다룹니다.
     */
    private final HttpServletResponse response;

    /**
     * 생성자 주입.
     *
     * @param response 스프링이 넣어 주는 "현재 요청의 HTTP 응답" 프록시
     */
    public ResponseAspect(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * {@code @Around}: 대상 메서드를 "감싸서" 실행 전후에 코드를 넣습니다.
     *
     * <p>괄호 안의 포인트컷 표현식은 "어떤 메서드에 적용할지" 입니다.
     * <ol>
     *   <li>{@code execution(public com.back.global.rsData.RsData *(..))}: 반환 타입이 RsData 인 public 메서드</li>
     *   <li>그중 {@code @Controller} 또는 {@code @RestController} 클래스 안에 있고
     *       ({@code @RestController} 는 안에 {@code @Controller} 를 품고 있지만, {@code within(@어노테이션 *)} 은
     *       그런 "어노테이션 위의 어노테이션"까지 따라가 보지 않으므로 둘 다 적어야 합니다)</li>
     *   <li>{@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping}, {@code @PatchMapping},
     *       {@code @DeleteMapping}, {@code @RequestMapping} 중 하나가 붙은 메서드</li>
     * </ol>
     *
     * @param joinPoint 감싸고 있는 실제 메서드. {@code proceed()} 로 실행
     * @return 실제 메서드의 반환값(RsData)을 그대로 돌려줌
     */
    @Around("""
                execution(public com.back.global.rsData.RsData *(..)) &&
                (
                    within(@org.springframework.stereotype.Controller *) ||
                    within(@org.springframework.web.bind.annotation.RestController *)
                ) &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PatchMapping) ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 실제 컨트롤러 메서드 실행
        Object result = joinPoint.proceed();

        // 2. 반환된 RsData 의 statusCode 로 HTTP 응답 상태 설정 (예: 201)
        RsData<?> rsData = (RsData<?>) result;
        response.setStatus(rsData.statusCode());

        // 3. 반환값은 바꾸지 않고 그대로 돌려줌 → 스프링이 JSON 으로 바꿔 응답 본문에 씀
        return result;
    }
}
