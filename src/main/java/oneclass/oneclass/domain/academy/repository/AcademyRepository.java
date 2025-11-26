package oneclass.oneclass.domain.academy.repository;

import oneclass.oneclass.domain.academy.entity.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademyRepository extends JpaRepository<Academy, String> {
    Optional<Academy> findByAcademyCode(String academyCode);
    Optional<Academy> findByAcademyCodeAndAcademyName(String academyCode, String academyName);
    Optional<Academy> findByEmail(String email);
    Optional<Academy> findByPhone(String phone);

    Optional<Academy> findByAcademyCodeAndStatus(String code, Academy.Status status);
    List<Academy> findByStatus(Academy.Status status);
}