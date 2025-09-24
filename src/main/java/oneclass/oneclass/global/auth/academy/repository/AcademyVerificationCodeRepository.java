package oneclass.oneclass.global.auth.academy.repository;

import oneclass.oneclass.global.auth.academy.entity.AcademyVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyVerificationCodeRepository extends JpaRepository<AcademyVerificationCode, String> {
    Optional<AcademyVerificationCode> findByAcademyCode(String academyCode);
}
