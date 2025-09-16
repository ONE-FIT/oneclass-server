package oneclass.oneclass.global.member.repository;

import oneclass.oneclass.global.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>  {
}
