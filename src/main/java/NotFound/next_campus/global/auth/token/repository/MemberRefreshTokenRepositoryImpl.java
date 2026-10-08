package NotFound.next_campus.global.auth.token.repository;

import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Transactional
public class MemberRefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final MemberRepository memberRepository;

    public MemberRefreshTokenRepositoryImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /* refresh 토큰 저장 */
    @Override
    public void save(Long studentId, String refreshToken, Duration ttl) {

        memberRepository.findByStudentId(studentId).ifPresent(m -> {
            m.setRefreshToken(refreshToken);
            m.setRefreshExpiry(LocalDateTime.now().plus(ttl));
        });
    }

    /* 학번으로 refreshToken 조회 */
    @Override
    public Optional<String> findByStudentId(Long studentId) {

        return memberRepository
                .findByStudentId(studentId)
                .map(Member::getRefreshToken);
    }

    /* refresh 토큰 삭제 */
    @Override
    public void delete(Long studentId) {

        Optional<Member> member = memberRepository.findByStudentId(studentId);

        if (member.isEmpty()) return;

        Member m = member.get();
        m.setRefreshToken(null);
        m.setRefreshExpiry(null);
    }
}
