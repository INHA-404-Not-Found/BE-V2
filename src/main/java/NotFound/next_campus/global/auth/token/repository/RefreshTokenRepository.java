package NotFound.next_campus.global.auth.token.repository;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {

    /* refresh 토큰 저장 */
    void save(Long studentId, String refreshToken, Duration ttl);

    /* 학번으로 refreshToken 조회 */
    Optional<String> findByStudentId(Long studentId);

    /* refresh 토큰 삭제 */
    void delete(Long studentId);
}
