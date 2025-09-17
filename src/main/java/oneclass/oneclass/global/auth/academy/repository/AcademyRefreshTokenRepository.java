package oneclass.oneclass.global.auth.academy.repository;

import oneclass.oneclass.global.auth.academy.entity.AcademyRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyRefreshTokenRepository extends JpaRepository<AcademyRefreshToken, Long> {
    Optional<AcademyRefreshToken> findByAcademyCode(String academyCode);
    void deleteByAcademyCode(String academyCode);

}
