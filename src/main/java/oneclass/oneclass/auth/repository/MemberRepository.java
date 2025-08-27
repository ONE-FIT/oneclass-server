package oneclass.oneclass.auth.repository;

import oneclass.oneclass.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
}
