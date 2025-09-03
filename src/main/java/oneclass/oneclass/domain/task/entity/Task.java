package oneclass.oneclass.domain.task.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.domain.auth.member.entity.Member;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
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

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssignment> assignments;

    private LocalDate dueDate; // 마감 기한

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus; // ASSIGNED / SUBMITTED / GRADED
}
