package NotFound.next_campus.domain.member.repository;

import NotFound.next_campus.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByStudentId(Long studentId);
}
