package oneclass.oneclass.global.auth.academy.repository;

import oneclass.oneclass.global.auth.academy.entity.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyRepository extends JpaRepository<Academy, String> {
    Optional<Academy> findByAcademyCode(String academyCode);
    Optional<Academy> findByAcademyCodeAndAcademyName(String academyCode, String academyName);
}