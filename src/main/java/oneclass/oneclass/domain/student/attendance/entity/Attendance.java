package oneclass.oneclass.domain.student.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.domain.auth.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Member student;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus; // PRESENT / ABSENT / LATE / EXCUSED

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}

