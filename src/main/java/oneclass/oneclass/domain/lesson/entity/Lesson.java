package oneclass.oneclass.domain.lesson.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.task.entity.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonId;
    private String title;

    // 수업을 만든 교사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")  // 단순 JoinColumn 사용
    private Member teacher;

    // 수업을 듣는 학생들

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Member> students = new HashSet<>();

    public void addStudent(Member student) {
        if (student.getLesson() != null && !student.getLesson().equals(this)) {
            student.getLesson().getStudents().remove(student);
        }
        student.setLesson(this);
        this.students.add(student);
    }
    // 수업에 속한 과제들
    @Builder.Default
    @OneToMany(mappedBy = "lesson")
    private List<Task> tasks = new ArrayList<>();
}
