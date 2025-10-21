package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    boolean existsByUsername(String username);
    Optional<RefreshToken> findByUsername(String username);

    // 토큰 단위 폐기(다중 세션 지원)
    boolean existsByUsernameAndToken(String username, String token);
    void deleteByUsernameAndToken(String username, String token);
}