package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    // 기존 단순 조회(필요 시 유지)
    Optional<VerificationCode> findTopByPhoneAndUsedFalseOrderByExpiryDesc(String phone);

    // 만료 필터링 포함(2 파라미터 버전)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findTopByPhoneAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
            String phone, LocalDateTime now);

    // 타입까지 포함한 만료 필터링(3 파라미터, 현재 서비스에서 사용할 메서드)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findTopByPhoneAndTypeAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
            String phone, VerificationCode.Type type, LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationCode> findTopByIdentifierAndTypeAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
            String identifier, VerificationCode.Type type, LocalDateTime now);

    void deleteByIdentifierAndType(String identifier, VerificationCode.Type type);
}