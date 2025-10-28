package oneclass.oneclass.domain.attendance.repository;

import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByMemberIdAndDate(Long memberId, LocalDate date);
    List<Attendance> findByMemberId(Long memberId);

    List<Attendance> findByDateAndAttendanceStatus(LocalDate date, AttendanceStatus status);

    List<Attendance> findByDate(LocalDate date);

    Optional<Attendance> findByMemberAndDate(Member member, LocalDate date);
}