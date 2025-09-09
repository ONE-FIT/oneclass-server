package oneclass.oneclass.global.auth.member.repository;

import oneclass.oneclass.global.auth.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>  {
}
