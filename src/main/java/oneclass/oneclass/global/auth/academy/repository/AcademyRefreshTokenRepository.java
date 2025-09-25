package oneclass.oneclass.global.auth.academy.repository;

import oneclass.oneclass.global.auth.academy.entity.AcademyRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRefreshTokenRepository extends JpaRepository<AcademyRefreshToken, Long> {


}
