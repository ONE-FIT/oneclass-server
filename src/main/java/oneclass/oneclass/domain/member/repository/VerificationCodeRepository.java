package oneclass.oneclass.domain.member.repository;

import jakarta.validation.constraints.NotBlank;
import oneclass.oneclass.domain.member.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByIdentifierAndTypeAndUsedFalseAndExpiryAfter(
            String identifier, VerificationCode.Type type, LocalDateTime now);

    Optional<VerificationCode> findTopByPhoneAndUsedFalseOrderByExpiryDesc(String phone);

    void deleteByIdentifierAndType(String identifier, VerificationCode.Type type);

    void deleteByPhone(String phone);
}
