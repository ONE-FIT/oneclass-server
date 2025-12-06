package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {




    // 만료 + 타입 필터 포함: phone + type 기준 (RESET_PASSWORD 등)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findTopByPhoneAndTypeAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
            String phone, VerificationCode.Type type, LocalDateTime now);

    // 만료 + 타입 필터 포함: identifier + type 기준 (ADMIN_EMAIL 등)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findTopByIdentifierAndTypeAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
            String identifier, VerificationCode.Type type, LocalDateTime now);

    // 식별자+타입으로 일괄 삭제(필요 시)
    void deleteByIdentifierAndType(String identifier, VerificationCode.Type type);

}