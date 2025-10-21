package oneclass.oneclass.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import oneclass.oneclass.domain.academy.entity.Academy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    // 선생 → 학생
    @ManyToMany
    @JoinTable(
            name = "teacher_student",
            joinColumns = @JoinColumn(name = "teacher_username", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "student_username", referencedColumnName = "username")
    )
    @JsonIgnore
    private List<Member> teachingStudents = new ArrayList<>();

    // 학생 → 선생
    @ManyToMany(mappedBy = "teachingStudents")
    @JsonIgnore
    private List<Member> teachers = new ArrayList<>();

    // 부모 → 자녀
    @ManyToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "parent_username", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "student_username", referencedColumnName = "username")
    )
    @JsonIgnore
    private List<Member> parentStudents = new ArrayList<>();

    // 자녀 → 부모
    @ManyToMany(mappedBy = "parentStudents")
    @JsonIgnore
    private List<Member> parents = new ArrayList<>();

    // 학원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_code", referencedColumnName = "academyCode")
    @JsonIgnore
    private Academy academy;

    @Builder
    private Member(Long id, String username, String password, String name,
                   String phone, String email, Role role,
                   List<Member> teachingStudents, List<Member> teachers,
                   List<Member> parentStudents, List<Member> parents,
                   Academy academy) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        if (teachingStudents != null) teachingStudents.forEach(this::addStudent);
        if (teachers != null) teachers.forEach(this::addTeacher);
        if (parentStudents != null) parentStudents.forEach(this::addParentStudent);
        if (parents != null) parents.forEach(this::addParent);
        this.academy = academy;
    }

    // ===== 편의 메서드(Teacher↔Student) =====
    public void addStudent(Member student) {
        if (!this.teachingStudents.contains(student)) {
            this.teachingStudents.add(student);
        }
        if (!student.teachers.contains(this)) {
            student.teachers.add(this);
        }
    }

    public void removeStudent(Member student) {
        this.teachingStudents.remove(student);
        student.teachers.remove(this);
    }

    public void addTeacher(Member teacher) {
        if (!this.teachers.contains(teacher)) {
            this.teachers.add(teacher);
        }
        if (!teacher.teachingStudents.contains(this)) {
            teacher.teachingStudents.add(this);
        }
    }

    public void removeTeacher(Member teacher) {
        this.teachers.remove(teacher);
        teacher.teachingStudents.remove(this);
    }

    // ===== 편의 메서드(Parent↔Child) =====
    public void addParentStudent(Member child) {
        if (!this.parentStudents.contains(child)) {
            this.parentStudents.add(child);
        }
        if (!child.parents.contains(this)) {
            child.parents.add(this);
        }
    }

    public void addParent(Member parent) {
        if (!this.parents.contains(parent)) {
            this.parents.add(parent);
        }
        if (!parent.parentStudents.contains(this)) {
            parent.parentStudents.add(this);
        }
    }
}