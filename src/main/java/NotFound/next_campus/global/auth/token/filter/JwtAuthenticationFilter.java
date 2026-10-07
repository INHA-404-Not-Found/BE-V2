package NotFound.next_campus.global.auth.token.filter;

import NotFound.next_campus.global.auth.token.exception.TokenException;
import NotFound.next_campus.global.auth.token.service.JwtTokenProvider;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.auth.user.CustomUserDetailsService;
import NotFound.next_campus.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - Authorization 헤더의 Bearer 토큰을 우선으로 확인
 * - 없으면 cookie에 저장된 ACCESS_TOKEN도 확인
 * - 토큰이 유효하면 Spring Security의 Authentication을 설정
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null) {
            // 1) 토큰이 없는 요청 (로그인 x)
            logger.debug("[JwtAuthenticationFilter] 토큰 없음");
        } else {
            try {
                // access 토큰 검증
                String studentId = tokenProvider.parseAccessToken(token).getSubject();
                // CustomUserDetails 사용
                CustomUserDetails user = customUserDetailsService.loadUserByUsername(studentId);

                // 2) 토큰이 유효한 요청
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (TokenException e) {
                // TokenException이 들고있는 ErrorCode를 EntryPoint로 넘김
                request.setAttribute("exception", e.getErrorCode());
            } catch (UsernameNotFoundException e) {
                request.setAttribute("exception", ErrorCode.TOKEN_INVALID);
            }
        }

        filterChain.doFilter(request, response);
    }

    // 토큰이 존재하는지 확인하는 메서드
    private String resolveToken(HttpServletRequest request) {

        // 1) Authorization header
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        // 2) cookie named ACCESS_TOKEN (웹에서 cookie로 사용시)
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(c.getName())) return c.getValue();
            }
        }

        return null;
    }
}