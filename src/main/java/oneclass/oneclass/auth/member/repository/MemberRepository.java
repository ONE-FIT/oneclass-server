package oneclass.oneclass.auth.member.repository;

import oneclass.oneclass.auth.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>  {
}
