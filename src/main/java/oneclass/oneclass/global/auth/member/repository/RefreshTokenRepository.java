package oneclass.oneclass.global.auth.member.repository;

import oneclass.oneclass.global.auth.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUsername(String username);

    Optional<RefreshToken> findByUsername(String username);
    boolean existsByUsername(String username);
}
