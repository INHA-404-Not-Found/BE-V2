package NotFound.next_campus.global.auth.token.api;

import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.global.auth.token.dto.request.LoginRequest;
import NotFound.next_campus.global.auth.token.dto.response.LoginResponse;
import NotFound.next_campus.global.auth.token.dto.request.RefreshRequest;
import NotFound.next_campus.global.auth.token.dto.response.ProfileResponse;
import NotFound.next_campus.global.auth.token.service.JwtTokenProvider;
import NotFound.next_campus.global.auth.token.service.MemberAuthService;
import NotFound.next_campus.global.auth.token.service.TokenService;
import NotFound.next_campus.global.auth.token.service.TokenService.LoginTokens;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


/**
 * 인증 관련 API 컨트롤러
 * - /auth/login : 로그인 (웹이면 쿠키에 refresh, 앱이면 JSON으로 둘 다 반환)
 * - /auth/refresh : refresh 토큰으로 access 토큰 재발급 (쿠키 우선, 바디로도 가능)
 * - /auth/logout : 로그아웃 (DB에서 refresh 토큰 제거, 쿠키 삭제)
 */
@RestController
@RequestMapping("/auth")
public class TokenController {
    private final TokenService tokenService;
    private final JwtTokenProvider tokenProvider;
    private final MemberAuthService memberAuthService;

    public TokenController(TokenService tokenService, JwtTokenProvider tokenProvider, MemberAuthService memberAuthService) {
        this.tokenService = tokenService;
        this.tokenProvider = tokenProvider;
        this.memberAuthService = memberAuthService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginTokens tokens = tokenService.login(req);


        if (req.getIsWeb()) {
        // 웹: refresh token을 HttpOnly 쿠키로 전달
            Cookie cookie = new Cookie("REFRESH_TOKEN", tokens.refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int)(tokenProvider.refreshTokenMillis / 1000)); // 초 단위
            cookie.setSecure(true); // 개발환경: false, 운영: true 및 HTTPS 사용
            response.addCookie(cookie);


            // access token은 JSON으로 전달 (웹에서는 프론트가 받아서 세션/메모리 관리)
            return ResponseEntity.ok(new LoginResponse(tokens.accessToken, null));
        }


        // 앱: access + refresh 토큰을 JSON으로 반환 (앱은 refresh를 Secure Storage에 저장)
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken, tokens.refreshToken));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "REFRESH_TOKEN", required = false) String cookieRefresh,
                                     @RequestBody(required = false) RefreshRequest body) {
        String refresh = cookieRefresh != null ? cookieRefresh : (body != null ? body.getRefreshToken() : null);
        LoginTokens tokens = tokenService.refresh(refresh);
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken, tokens.refreshToken));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "REFRESH_TOKEN", required = false) String cookieRefresh,
                                    @RequestBody(required = false) RefreshRequest body,
                                    HttpServletResponse response) {
        String refresh = cookieRefresh != null ? cookieRefresh : (body != null ? body.getRefreshToken() : null);
        tokenService.logout(refresh);


        // 웹 쿠키 제거
        Cookie cookie = new Cookie("REFRESH_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);


        return ResponseEntity.ok().body("logged out");
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse profile = memberAuthService.getProfile(userDetails);
        return ResponseEntity.ok(profile);
    }

}