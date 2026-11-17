package com.back;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 가장 기본적인 스모크 테스트입니다.
 *
 * <p>{@code @SpringBootTest} 는 실제 애플리케이션과 똑같이 스프링 컨테이너 전체를 띄웁니다.
 * 테스트 메서드가 비어 있어도, 컨테이너를 띄우는 도중 설정 오류나 빈 생성 오류가 있으면 실패합니다.
 * 즉 "앱이 최소한 켜지기는 하는가?" 를 확인합니다.
 */
@SpringBootTest
class BackApplicationTests {

	/** 스프링 컨텍스트(컨테이너)가 오류 없이 로드되는지 확인합니다. */
	@Test
	void contextLoads() {
	}

}
