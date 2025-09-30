package oneclass.oneclass.domain.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.domain.member.entity.Member;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
    private TaskStatus taskStatus; // ASSIGNED / SUBMITTED
}