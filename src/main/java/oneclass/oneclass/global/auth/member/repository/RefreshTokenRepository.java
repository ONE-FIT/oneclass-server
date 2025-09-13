package oneclass.oneclass.global.auth.member.repository;

import oneclass.oneclass.global.auth.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUsername(String username);
}
