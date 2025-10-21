package oneclass.oneclass.domain.attendance.repository.jpa;

import oneclass.oneclass.domain.attendance.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    @Modifying
    @Query("UPDATE QrCode c SET c.valid=false WHERE c.userId=:userId")
    void updateAllInvalidCheckCode(@Param("userId") Long userId);

    boolean existsByCodeAndValid(String code, boolean valid);
}
