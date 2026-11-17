// Gradle 빌드 설정 파일 (Kotlin DSL)
// 이 프로젝트를 어떻게 컴파일하고, 어떤 라이브러리를 쓰고, 테스트를 어떻게 돌릴지 정합니다.

plugins {
	// 자바 프로젝트로 빌드 (compileJava, test, jar 같은 작업이 생김)
	java
	// 스프링 부트 플러그인: bootRun(앱 실행), bootJar(실행 가능한 jar 만들기) 작업 제공
	id("org.springframework.boot") version "4.1.2"
	// 의존성 버전 관리: 아래 dependencies 에서 버전을 안 적어도 스프링 부트가 검증한 버전을 넣어줌
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "rest-api-2"

java {
	toolchain {
		// 이 PC에 설치된 자바 버전과 상관없이 Java 25 로 컴파일·실행
		// (없으면 Gradle 이 ~/.gradle/jdks 에 내려받아 사용)
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	// 라이브러리를 내려받을 저장소
	mavenCentral()
}

dependencies {
	// implementation: 컴파일과 실행 모두에 필요
	// webmvc: 내장 톰캣 + 스프링 MVC(@RestController, JSON 변환 등)
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	// data-jpa: JPA(Hibernate) + Spring Data JPA. 자바 객체(엔티티)를 DB 테이블에 저장/조회
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// validation: 요청 값 검증(@NotBlank, @Size 등)
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// h2console: 브라우저에서 H2 DB 내용을 볼 수 있는 웹 콘솔(/h2-console)
	implementation("org.springframework.boot:spring-boot-h2console")
	// runtimeOnly: 실행할 때만 필요. H2 는 자바로 만든 가벼운 DB (파일 또는 메모리에 저장)
	runtimeOnly("com.h2database:h2")

	// compileOnly + annotationProcessor: 컴파일할 때만 필요한 라이브러리
	// 롬복은 @Getter, @RequiredArgsConstructor 같은 어노테이션을 보고 컴파일 시점에 코드를 만들어 줌
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// developmentOnly: 개발 중 실행(bootRun)할 때만 포함, 배포용 jar 에는 빠짐
	// devtools: 코드가 바뀌면 앱을 자동으로 재시작
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// test 로 시작하는 것들은 테스트 코드에서만 사용
	// webmvc-test: JUnit, AssertJ, MockMvc 등 웹 테스트 도구 묶음
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	// 테스트 실행기로 JUnit 5 (JUnit Platform) 사용
	useJUnitPlatform()
}
