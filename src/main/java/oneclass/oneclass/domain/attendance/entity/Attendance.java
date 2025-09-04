package oneclass.oneclass.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@ToString(exclude = "member") // 무한 루프 방지
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Member member;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus; // PRESENT / ABSENT / LATE / EXCUSED

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}

