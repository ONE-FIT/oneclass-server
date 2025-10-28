package oneclass.oneclass.domain.attendance.repository;

import oneclass.oneclass.domain.attendance.entity.AttendanceNonce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceNonceRepository extends JpaRepository<AttendanceNonce, Long> {

    Optional<AttendanceNonce> findByNonce(String nonce);
}