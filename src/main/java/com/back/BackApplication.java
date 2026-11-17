package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 애플리케이션의 시작점(진입점)입니다.
 *
 * <p>{@code @SpringBootApplication} 은 아래 세 어노테이션을 합친 것입니다.
 * <ul>
 *   <li>{@code @SpringBootConfiguration}: 이 클래스가 스프링 설정 클래스임을 표시</li>
 *   <li>{@code @EnableAutoConfiguration}: 의존성(build.gradle.kts)을 보고 필요한 설정을 자동으로 등록
 *       (예: webmvc 가 있으면 내장 톰캣과 DispatcherServlet 을 자동으로 준비)</li>
 *   <li>{@code @ComponentScan}: 이 클래스가 있는 패키지(com.back)와 그 하위 패키지에서
 *       {@code @Component}, {@code @Service}, {@code @RestController} 등을 찾아 빈으로 등록</li>
 * </ul>
 * 그래서 이 클래스는 항상 최상위 패키지(com.back)에 두어야 하위 패키지의 클래스들이 모두 스캔됩니다.
 *
 * <p>{@code @EnableJpaAuditing} 은 JPA Auditing 기능을 켭니다.
 * 이게 있어야 BaseEntity 의 {@code @CreatedDate}, {@code @LastModifiedDate} 필드에
 * 저장·수정 시각이 자동으로 채워집니다.
 */
@SpringBootApplication
@EnableJpaAuditing
public class BackApplication {

	/**
	 * 자바 프로그램의 main 메서드입니다.
	 * {@code SpringApplication.run} 이 스프링 컨테이너를 만들고, 빈을 등록하고,
	 * 내장 웹 서버(기본 8080 포트)를 띄웁니다.
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}

}
