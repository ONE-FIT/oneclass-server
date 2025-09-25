package oneclass.oneclass.global.auth.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import oneclass.oneclass.global.auth.academy.entity.Academy;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    // 학생 -> 선생 (다:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @JsonIgnore
    private Member teacher;

    // 선생 -> 학생 (1:다)
    @OneToMany(mappedBy = "teacher")
    @JsonIgnore
    private List<Member> teacherStudents = new ArrayList<>();

    // 부모 -> 자녀 (owning side)
    @ManyToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "parent_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnore
    private List<Member> parentStudents = new ArrayList<>();

    // 자녀 -> 부모 (inverse)
    @ManyToMany(mappedBy = "parentStudents")
    @JsonIgnore
    private List<Member> parents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_code", referencedColumnName = "academyCode")
    @JsonIgnore
    private Academy academy;

    @Builder
    private Member(Long id, String username, String password, String name,
                   String phone, String email, Role role,
                   Member teacher, List<Member> teacherStudents,
                   List<Member> parentStudents, List<Member> parents,
                   Academy academy) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        if (teacher != null) setTeacher(teacher);
        if (teacherStudents != null) teacherStudents.forEach(this::addTeacherStudent);
        if (parentStudents != null) parentStudents.forEach(this::addParentStudent);
        if (parents != null) parents.forEach(this::addParent);
        this.academy = academy;
    }

    // 관계 편의 메서드
    public void setTeacher(Member teacher) {
        this.teacher = teacher;
        if (teacher != null && !teacher.teacherStudents.contains(this)) {
            teacher.teacherStudents.add(this);
        }
    }

    public void addTeacherStudent(Member student) {
        if (!this.teacherStudents.contains(student)) {
            this.teacherStudents.add(student);
            student.setTeacher(this);
        }
    }

    public void addParentStudent(Member child) {
        if (!this.parentStudents.contains(child)) {
            this.parentStudents.add(child);
            if (!child.parents.contains(this)) {
                child.parents.add(this);
            }
        }
    }

    public void addParent(Member parent) {
        if (!this.parents.contains(parent)) {
            this.parents.add(parent);
            if (!parent.parentStudents.contains(this)) {
                parent.parentStudents.add(this);
            }
        }
    }

    public void changePassword(String encodedPw) {
        this.password = encodedPw;
    }
}