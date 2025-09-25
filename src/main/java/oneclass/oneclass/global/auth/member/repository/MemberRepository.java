package oneclass.oneclass.global.auth.member.repository;

import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.global.auth.member.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmailOrPhone(String email, String phone);

    @Query("select m.phone from Member m")
    Page<String> findAllPhones(Pageable pageable);

    Role role(Role role);
}
