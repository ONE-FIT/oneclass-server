package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmailOrPhone(String email, String phone);

    @Query("select m.phone from Member m")
    Page<String> findAllPhones(Pageable pageable);

    @Query("""
    SELECT m FROM Member m
    WHERE NOT EXISTS (
        SELECT 1 FROM Attendance a
        WHERE a.member.id = m.id
          AND a.date = :date
          AND a.attendanceStatus IN ('PRESENT', 'LATE', 'EXCUSED')
    )
    """)
    List<Member> findAbsentMembers(@Param("date") LocalDate date);
}
