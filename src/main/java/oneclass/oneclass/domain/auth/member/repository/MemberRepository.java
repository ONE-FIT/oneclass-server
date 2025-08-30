package oneclass.oneclass.domain.auth.member.repository;

import oneclass.oneclass.domain.auth.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
