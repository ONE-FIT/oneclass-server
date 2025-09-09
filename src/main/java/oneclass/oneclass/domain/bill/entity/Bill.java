package oneclass.oneclass.domain.bill.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.time.LocalDate;

@Entity
@Data
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Member student;

    private int amount;
    private String description; // 예: "3월 수강료, 교재비"
    private LocalDate issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private BillStatus status; // ISSUED / PAID / CANCELED
}

