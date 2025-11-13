package oneclass.oneclass.domain.attendance.repository;

import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByMemberIdAndDate(Long memberId, LocalDate date);
    List<Attendance> findByMemberId(Long memberId);

    List<Attendance> findByDateAndAttendanceStatus(LocalDate date, AttendanceStatus status);

    List<Attendance> findByDate(LocalDate date);

    Optional<Attendance> findByMemberAndDate(Member member, LocalDate date);

    List<Attendance> findAllByLessonId(Long lessonId);

    List<Attendance> findByLessonIdAndDate(Long lessonId, LocalDate date);

    List<Attendance> findByLessonIdAndDateAndAttendanceStatus(Long lessonId, LocalDate date, AttendanceStatus status);

    @Query("""
    SELECT a FROM Attendance a
    JOIN a.member m
    JOIN m.academy ac
    WHERE ac.academyCode = :academyId AND a.date = :date
""")
    List<Attendance> findByAcademyAndDate(@Param("academyId") Long academyId, @Param("date") LocalDate date);
}