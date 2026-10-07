package NotFound.next_campus.global.auth.token.filter;

import NotFound.next_campus.global.common.CommonResponse;
import NotFound.next_campus.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증이 안 된 상태로 인증이 필요한 요청을 보냈을 때 (401)
 * @RestControllerAdvice는 필터 체인에서 발생하는 예외를 잡지 못하므로 별도로 처리
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object attr = request.getAttribute("exception");
        ErrorCode errorCode = (attr instanceof ErrorCode code) ? code : ErrorCode.TOKEN_INVALID;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        CommonResponse.fail(errorCode)
                )
        );
    }
}
