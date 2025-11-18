package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByUsername(String username);

    boolean existsByUsernameAndToken(String username, String token);

    void deleteByUsernameAndToken(String username, String token);
}