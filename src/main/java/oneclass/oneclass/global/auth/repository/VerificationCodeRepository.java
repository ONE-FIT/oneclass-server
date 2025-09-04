package oneclass.oneclass.global.auth.repository;

import oneclass.oneclass.global.auth.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
}
