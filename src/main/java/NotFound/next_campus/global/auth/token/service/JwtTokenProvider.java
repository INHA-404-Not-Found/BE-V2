package NotFound.next_campus.global.auth.token.service;

import NotFound.next_campus.global.auth.token.exception.TokenException;
import NotFound.next_campus.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성/검증 유틸
 * - secret은 application.properties 또는 환경변수로 지정
 * - access/refresh 만료시간(ms)도 프로퍼티로 주입
 */
@Component
public class JwtTokenProvider {

    private final String issuer;
    private final SecretKey secretKey;

    @Getter
    private final long accessTokenMillis;
    @Getter
    private final long refreshTokenMillis;

    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    public JwtTokenProvider(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenMillis,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenMillis
    ) {
        this.issuer = issuer;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenMillis = accessTokenMillis;
        this.refreshTokenMillis = refreshTokenMillis;
    }

    // Access Token 생성
    public String createAccessToken(Long studentId){

        return createToken(studentId, ACCESS_TYPE, accessTokenMillis);
    }

    public String createRefreshToken(Long studentId) {

        return createToken(studentId, REFRESH_TYPE, refreshTokenMillis);
    }

    // AccessToken 검증 - JwtAuthenticationFilter가 요청마다 호출
    public Claims parseAccessToken(String token) {

        return parse(token, ACCESS_TYPE);
    }

    // RefreshToken 검증
    public Claims parseRefreshToken(String token) {

        return parse(token, REFRESH_TYPE);
    }

    private String createToken(Long studentId, String type, long validityMillis) {

        Date now = new Date();

        return Jwts.builder()
                .header().type(type).and()
                .subject(String.valueOf(studentId))
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validityMillis))
                .signWith(secretKey)
                .compact();
    }

    // 토큰 유효성 검증
    private Claims parse(String token, String expectedType) {

        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)      // 우리가 발급한 토큰인가
                    .build()
                    .parseSignedClaims(token);  // 서명, 만료 검증

            /* *** 중요 ***
             * 기존 취약점: refreshToken을 accessToken으로도 사용 가능한 문제 발견
             * 따라서, 토큰의 타입 검증을 통해 이러한 문제를 해결 */
            if(!expectedType.equals(jws.getHeader().getType())) {
                throw new TokenException(ErrorCode.TOKEN_INVALID);
            }

            // header.payload.signature
            return jws.getPayload();
        } catch(ExpiredJwtException e) {
            throw new  TokenException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenException(ErrorCode.TOKEN_INVALID);
        }
    }
}