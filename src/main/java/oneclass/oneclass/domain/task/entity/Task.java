package oneclass.oneclass.domain.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.time.LocalDate;
import java.util.List;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private Member teacher; // 출제자

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssignment> assignments;

    private LocalDate dueDate; // 마감 기한

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus; // ASSIGNED / SUBMITTED / GRADED

//    @ManyToOne
//    @JoinColumn(name = "lesson_id") // FK 컬럼 지정
//    private Lesson lessonId; // <-- 이 필드가 있어야 함
}
