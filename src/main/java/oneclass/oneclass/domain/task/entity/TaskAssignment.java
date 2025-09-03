package oneclass.oneclass.domain.task.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.domain.auth.member.entity.Member;

@Entity
@Data
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 과제인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    // 어떤 학생인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Member student;

    // 학생별 과제 상태
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus; // ASSIGNED / SUBMITTED / GRADED
}
