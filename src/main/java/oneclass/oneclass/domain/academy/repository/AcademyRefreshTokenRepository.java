package oneclass.oneclass.domain.academy.repository;

import oneclass.oneclass.domain.academy.entity.AcademyRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AcademyRefreshTokenRepository extends JpaRepository<AcademyRefreshToken, Long> {

    Optional<AcademyRefreshToken> findByAcademyCode(String academyCode);

    boolean existsByAcademyCode(String academyCode);

    @Modifying
    @Query("DELETE FROM AcademyRefreshToken art WHERE art.academyCode = :academyCode")
    void deleteByAcademyCode(String academyCode);
}