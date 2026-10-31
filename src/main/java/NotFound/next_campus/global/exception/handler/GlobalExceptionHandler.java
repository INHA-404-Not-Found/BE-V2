package NotFound.next_campus.global.exception.handler;

import NotFound.next_campus.global.auth.token.exception.TokenException;
import NotFound.next_campus.global.common.CommonResponse;
import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 핸들러
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<CommonResponse<Void>> handleBadRequest(Exception e) {
        log.warn("[BadRequest] {}", e.getMessage());
        return ResponseEntity.badRequest().body(CommonResponse.fail(ErrorCode.INVALID_INPUT));
    }

    // 405 핸들러
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("[MethodNotAllowed] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(CommonResponse.fail(ErrorCode.METHOD_NOT_ALLOWED));
    }

    // 404 핸들러
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleNoResource(NoResourceFoundException e) {
        log.warn("[NotFound] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.fail(ErrorCode.RESOURCE_NOT_FOUND));
    }

    // 서비스 계층에서 던지는 모든 비즈니스 예외 -> ErrorCode에 정의된 상태/코드/메시지 그대로 응답
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[BusinessException] {} - {}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(CommonResponse.fail(errorCode));
    }

    // 기존 global/auth/token/exception/RestExceptionHandler가 담당하던 TokenException 처리를 흡수
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<CommonResponse<Void>> handleTokenException(TokenException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[TokenException] {}", e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(CommonResponse.fail(errorCode));
    }

    // 서비스 로직에서 직접 던지는 AccessDeniedException (SecurityConfig의 인가 규칙이 아니라 도메인 권한 체크에서 발생)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("[AccessDeniedException] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.fail(ErrorCode.FORBIDDEN));
    }

    // 로그인 시 AuthenticationManager.authenticate()가 던지는 예외 (아이디/비밀번호 불일치 등)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getStatus())
                .body(CommonResponse.fail(ErrorCode.INVALID_CREDENTIALS));
    }

    // 예상하지 못한 나머지 모든 예외 -> 500. 스택트레이스는 로그로만 남기고 클라이언트에는 노출하지 않음
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("[Unhandled Exception]", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
