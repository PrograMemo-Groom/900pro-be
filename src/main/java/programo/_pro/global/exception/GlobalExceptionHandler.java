package programo._pro.global.exception;

import jakarta.mail.MessagingException;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import programo._pro.global.ApiResponse;

@Slf4j
@RestControllerAdvice
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "false", matchIfMissing = false)
public class GlobalExceptionHandler {

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        ApiResponse<?> response = ApiResponse.fail("알 수 없는 오류 요청 URL을 다시 확인해보십시오: " + ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 이메일 중복 인증 발생 예외 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException ex) {
        ApiResponse<?> response = ApiResponse.fail(ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 파라미터로 받은 값의 인자가 맞지 않을 때
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("잘못된 요청: '%s' 값을 '%s' 타입으로 변환할 수 없습니다.", e.getValue(), e.getRequiredType().getSimpleName());
        return ApiResponse.fail(message);
    }

    // 숫자 변환 실패
    @ExceptionHandler(NumberFormatException.class)
    public ApiResponse<?> handleNumberFormatException(NumberFormatException e) {
        return ApiResponse.fail("숫자 변환 오류: " + e.getMessage());
    }

    // 데이터베이스 접근 오류
    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<?> handleDataAccessException(DataAccessException e) {
        return ApiResponse.fail("데이터베이스 오류 발생: " + e.getMessage());
    }

    // 요구되는 값이 비어있을 때
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<?>> handleNullPointerException(NullPointerException ex) {
        ApiResponse<?> response = ApiResponse.fail("필수 데이터가 누락되었습니다.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 유효하지 않은 요청 파라미터
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<?> response = ApiResponse.fail("잘못된 요청: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 특정 예외를 명확히 처리
    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResultException(NoResultException ex) {
        ApiResponse<?> response = ApiResponse.fail("데이터를 찾을 수 없습니다.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleEmptyResultException(EmptyResultDataAccessException ex) {
        ApiResponse<?> response = ApiResponse.fail("결과가 존재하지 않습니다.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ApiResponse<?>> handleMessagingException(MessagingException ex) {
        ApiResponse<?> response = ApiResponse.fail(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 채팅 관련 예외 처리
    @ExceptionHandler(NotFoundChatException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFoundChatException(NotFoundChatException ex) {
        ApiResponse<?> response = ApiResponse.fail(ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

}
