package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.username = :username")
    void deleteByUsername(String username);

    Optional<RefreshToken> findByUsername(String username);
    boolean existsByUsername(String username);
}
