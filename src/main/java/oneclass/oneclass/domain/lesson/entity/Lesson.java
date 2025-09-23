package oneclass.oneclass.domain.lesson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lid;

    private String title;

    // 수업을 만든 교사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")  // 단순 JoinColumn 사용
    private Member teacher;

    // 수업을 듣는 학생들
    @ManyToMany
    @JoinTable(
            name = "lesson_student",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Member> students = new ArrayList<>();

    // 수업에 속한 과제들
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
}
