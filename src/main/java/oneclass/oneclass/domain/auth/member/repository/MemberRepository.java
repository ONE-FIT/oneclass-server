package oneclass.oneclass.domain.auth.member.repository;

import oneclass.oneclass.domain.auth.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmailOrPhone(String email, String phone);
}
