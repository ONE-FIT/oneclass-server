package oneclass.oneclass.attendance.repository.jpa;

import oneclass.oneclass.attendance.entity.QrCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QrCodeRepository extends JpaRepository<QrCodeEntity, Long> {
    @Modifying
    @Query("UPDATE QrCodeEntity c SET c.valid=false WHERE c.userId=:userId")
    void updateAllInvalidCheckCode(@Param("userId") Long userId);

    boolean existsByCodeAndValid(String code, boolean valid);
}
