package NotFound.next_campus.global.auth.token.service;

import NotFound.next_campus.global.auth.token.dto.TokenDTO;
import NotFound.next_campus.global.auth.token.dto.request.LoginRequest;
import NotFound.next_campus.global.auth.token.exception.TokenException;
import NotFound.next_campus.global.auth.token.repository.RefreshTokenRepository;
import NotFound.next_campus.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(AuthenticationManager authenticationManager,
                        JwtTokenProvider jwtTokenProvider,
                        RefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public TokenDTO login(LoginRequest req) {

        // 1) 인증 수행 (UserDetailsService와 PasswordEncoder로 검증)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getStudentId(), req.getPassword())
        );

        // 2) 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(req.getStudentId());
        String refreshToken = jwtTokenProvider.createRefreshToken(req.getStudentId());

        refreshTokenRepository.save(
                req.getStudentId(),
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenMillis())
        );

        return new TokenDTO(accessToken, refreshToken);
    }

    @Transactional
    public TokenDTO refresh(String refreshToken) {

        // refresh 토큰 유효성 검증
        Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);

        Long studentId = Long.valueOf(claims.getSubject());

        // DB에 저장된 refresh 토큰과 비교
        String stored = refreshTokenRepository.findByStudentId(studentId)
                .orElseThrow(() -> new TokenException(ErrorCode.TOKEN_NOT_FOUND));
        if (!stored.equals(refreshToken)) {
            throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
        }

        // 새 access 토큰 발급
        String newAccess = jwtTokenProvider.createAccessToken(
                studentId
        );

        return new TokenDTO(newAccess, refreshToken);
    }


    @Transactional
    public void logout(String refreshToken) {

        try {
            // refresh token 유효성 검사
            Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);

            Long studentId = Long.valueOf(claims.getSubject());

            // DB에 저장된 refresh 토큰과 비교
            String stored = refreshTokenRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new TokenException(ErrorCode.TOKEN_NOT_FOUND));
            if (!stored.equals(refreshToken)) {
                throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
            }

            refreshTokenRepository.delete(studentId);
        } catch (TokenException e) {
            log.debug("[TokenException] 로그아웃 실패: " + e.getMessage());
        }
    }
}