package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByPhone(String phone);

    // 토큰 단위 폐기(다중 세션 지원 시 유용)
    boolean existsByPhoneAndToken(String phone, String token);
    void deleteByPhoneAndToken(String phone, String token);
}