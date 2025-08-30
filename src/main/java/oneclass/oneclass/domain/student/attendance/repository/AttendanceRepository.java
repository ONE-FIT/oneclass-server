package oneclass.oneclass.domain.student.attendance.repository;

import oneclass.oneclass.domain.student.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}
