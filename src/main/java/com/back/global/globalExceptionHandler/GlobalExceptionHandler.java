package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 컨트롤러에서 예외가 터졌을 때, 그 예외를 잡아서 RsData 형식의 오류 응답으로 바꿔 주는 곳입니다.
 *
 * <p>{@code @ControllerAdvice}: 모든 컨트롤러에 공통으로 적용되는 설정 클래스.
 * 안의 {@code @ExceptionHandler(예외클래스)} 메서드가 해당 예외를 처리합니다.
 * 덕분에 컨트롤러마다 try-catch 를 쓰지 않아도 됩니다.
 *
 * <p>여기서는 {@code ResponseEntity} 로 상태코드를 직접 지정합니다.
 * (ResponseAspect 는 컨트롤러의 매핑 메서드에만 적용되므로 예외 처리 메서드에는 적용되지 않음)
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 없는 데이터를 요청했을 때 → 404.
     *
     * <p>{@code Optional.get()} 은 값이 없으면 NoSuchElementException 을 던집니다.
     * 예를 들어 {@code postService.findById(999).get()} 처럼 없는 글을 찾으면 이 메서드가 처리합니다.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "404-1",
                        "해당 데이터가 존재하지 않습니다."
                ),
                NOT_FOUND
        );
    }

    /**
     * {@code @Valid} 검증에 실패했을 때 → 400.
     *
     * <p>예: 제목이 비어 있으면 @NotBlank 위반. 위반한 필드마다
     * "필드명-규칙명-기본메시지" 형태의 줄을 만들고, 정렬해서 줄바꿈으로 이어 붙입니다.
     * 예) {@code title-NotBlank-공백일 수 없습니다}
     * (기본메시지는 실행 환경의 언어 설정에 따라 한국어/영어로 달라질 수 있음)
     * 정렬하는 이유: 위반 순서가 실행할 때마다 달라질 수 있어서, 항상 같은 메시지가 나오게 하려고.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getAllErrors()               // 모든 검증 오류
                .stream()
                .filter(error -> error instanceof FieldError) // 필드 단위 오류만
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        msg
                ),
                BAD_REQUEST
        );
    }

    /**
     * 요청 본문을 읽을 수 없을 때 → 400.
     * 예: JSON 문법이 틀렸거나(쉼표 빠짐 등) 본문이 아예 없을 때.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        "요청 본문이 올바르지 않습니다."
                ),
                BAD_REQUEST
        );
    }
}
