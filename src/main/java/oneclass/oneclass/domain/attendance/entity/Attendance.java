package oneclass.oneclass.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import oneclass.oneclass.domain.member.entity.Member;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;
}