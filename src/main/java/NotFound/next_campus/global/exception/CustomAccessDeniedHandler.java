package NotFound.next_campus.global.exception;

import NotFound.next_campus.global.common.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 됐지만 인가 규칙(hasRole 등)에 막혔을 때 (403)
 * 현재 SecurityConfig에는 경로 기반 permitAll/authenticated만 있고 hasRole 규칙이 없어
 * 실제로는 도메인 권한 체크(AccessDeniedException을 서비스에서 직접 던지는 경우)가 더 많고,
 * 그 경우는 GlobalExceptionHandler가 처리한다. 이 핸들러는 향후 hasRole 등을 추가할 때를 대비한 것.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(ErrorCode.FORBIDDEN.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(CommonResponse.fail(ErrorCode.FORBIDDEN))
        );
    }
}
