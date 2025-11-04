package oneclass.oneclass.domain.attendance.repository;

import oneclass.oneclass.domain.attendance.entity.AttendanceNonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceNonceRepository extends JpaRepository<AttendanceNonce, Long> {
    Optional<AttendanceNonce> findByNonce(String nonce);

    @Query("SELECT DISTINCT a.lessonId FROM AttendanceNonce a WHERE a.expireAt > :now AND a.used = false")
    List<Long> findActiveLessonIds(@Param("now") LocalDateTime now);
}