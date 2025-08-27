package oneclass.oneclass.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import oneclass.oneclass.auth.entity.Member;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private LocalDate date; // 출석 날짜

    private boolean present; // true: 출석, false: 결석
}
