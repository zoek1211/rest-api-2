package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 모든 API 가 공통으로 쓰는 응답 형식입니다. (RsData = Result Data)
 *
 * <p>JSON 으로 바뀌면 이런 모양이 됩니다.
 * <pre>
 * {
 *   "resultCode": "201-1",
 *   "msg": "4번 글이 작성되었습니다.",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <ul>
 *   <li>{@code resultCode}: "HTTP상태코드-세부번호" 형식. 예) 201-1, 400-1, 404-1.
 *       같은 상태코드 안에서도 상황을 구분하고 싶을 때 뒤 번호를 바꿉니다.</li>
 *   <li>{@code msg}: 사람이 읽는 결과 메시지</li>
 *   <li>{@code data}: 실제 결과 데이터. 없으면 null</li>
 * </ul>
 *
 * <p>{@code record} 는 자바 16+ 문법으로, 필드·생성자·getter(resultCode() 처럼 필드 이름 그대로)·
 * equals·hashCode·toString 을 자동으로 만들어 주는 "값을 담는 클래스" 입니다.
 * {@code <T>} 는 제네릭으로, data 에 어떤 타입이든 담을 수 있게 합니다.
 *
 * @param resultCode 결과 코드 (예: "201-1")
 * @param statusCode HTTP 상태코드. resultCode 앞자리에서 자동으로 계산됨
 * @param msg        결과 메시지
 * @param data       결과 데이터
 */
public record RsData<T>(
        String resultCode,
        // @JsonIgnore: JSON 응답에는 넣지 않음. 상태코드는 HTTP 응답 상태줄로 이미 전달되기 때문
        // (ResponseAspect 가 이 값을 HTTP 상태코드로 설정함)
        @JsonIgnore int statusCode,
        String msg,
        T data
) {
    /** data 가 없는 응답(예: 삭제 성공)을 만들 때 쓰는 생성자. data 는 null. */
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    /**
     * statusCode 를 직접 주지 않아도 되게 하는 생성자.
     * "201-1" 을 "-" 기준으로 최대 2조각으로 나눠 앞의 "201" 을 숫자로 바꿔 statusCode 로 씁니다.
     * 그래서 resultCode 앞자리와 HTTP 상태코드가 항상 같아집니다.
     */
    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }
}
