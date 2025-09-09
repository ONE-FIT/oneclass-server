package oneclass.oneclass.domain.attendance.repository;

import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMemberId(Long memberId);

    List<Attendance> findByDateAndAttendanceStatus(LocalDate date, AttendanceStatus status);

    List<Attendance> findByDate(LocalDate date);
}
