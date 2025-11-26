package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
    Optional<VerificationCode> findTopByIdentifierAndTypeAndUsedFalseAndExpiryAfter(
            String identifier, VerificationCode.Type type, LocalDateTime now);

    Optional<VerificationCode> findTopByIdentifierAndTypeOrderByExpiryDesc(
            String identifier, VerificationCode.Type type);

    void deleteByIdentifierAndType(String identifier, VerificationCode.Type type);
}
