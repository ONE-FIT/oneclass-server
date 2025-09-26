package oneclass.oneclass.global.auth.member.repository;

import oneclass.oneclass.global.auth.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUsername(String username);
    boolean existsByUsername(String username);
    // deleteById(username) 사용 가능 (username이 PK일 때)
}