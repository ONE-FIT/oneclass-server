package oneclass.oneclass.global.auth.member.repository;

import oneclass.oneclass.global.auth.member.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
}
