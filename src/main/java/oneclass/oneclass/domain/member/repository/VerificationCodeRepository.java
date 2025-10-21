package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
}
