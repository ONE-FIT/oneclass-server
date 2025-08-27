package oneclass.oneclass.attendance;

import oneclass.oneclass.attendance.entity.AttendanceEntity;
import oneclass.oneclass.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByDate(LocalDate date);
    Optional<AttendanceEntity> findByMemberAndDate(Member member, LocalDate date);
}
