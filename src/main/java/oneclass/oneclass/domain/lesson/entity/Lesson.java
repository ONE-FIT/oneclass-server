package oneclass.oneclass.domain.lesson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @OneToMany(mappedBy = "lesson")
    private Set<Member> students = new HashSet<>();
    public void addStudent(Member student) {
        // 학생이 이미 다른 수업에 속해 있는 경우, 이전 수업에서 학생을 제거합니다.
        if (student.getLesson() != null && !student.getLesson().equals(this)) {
            student.getLesson().getStudents().remove(student);
        }
        // 학생의 수업 정보를 현재 수업으로 설정합니다. (연관관계의 주인)
        student.setLesson(this);
        // Set은 중복을 허용하지 않으므로, contains 확인 없이 바로 추가할 수 있습니다.
        this.students.add(student);
    }

    // 수업에 속한 과제들
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
}
