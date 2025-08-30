package oneclass.oneclass.domain.auth.member.repository;

import oneclass.oneclass.domain.auth.member.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
}
