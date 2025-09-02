package oneclass.oneclass.member.repository;

import oneclass.oneclass.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>  {
}
