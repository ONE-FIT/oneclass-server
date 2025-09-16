package oneclass.oneclass.domain.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.global.member.entity.Member;

import java.time.LocalDate;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Table(name =  "Task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private Member assignedBy; // 출제자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private Member assignedTo; // 대상 학생

    private LocalDate dueDate; // 마감 기한

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // ASSIGNED / SUBMITTED / GRADED
}
