package oneclass.oneclass.domain.common.member.repository;

import oneclass.oneclass.domain.common.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
