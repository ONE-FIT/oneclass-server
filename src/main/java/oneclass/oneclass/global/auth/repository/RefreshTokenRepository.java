package oneclass.oneclass.global.auth.repository;

import oneclass.oneclass.global.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUsername(String username);
}
