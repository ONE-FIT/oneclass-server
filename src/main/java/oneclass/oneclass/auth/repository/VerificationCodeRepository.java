package oneclass.oneclass.auth.repository;

import oneclass.oneclass.auth.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
}
