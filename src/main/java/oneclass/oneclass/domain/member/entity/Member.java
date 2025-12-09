package oneclass.oneclass.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.lesson.entity.Lesson;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저네임: 로그인 / 표시명 분리하려면 displayName 추가 고려
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank
    private String username;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    @NotBlank
    private String name;

    @Column(nullable = false, unique = true, length = 13)
    @NotBlank
    private String phone;


    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_code", referencedColumnName = "academyCode")
    @JsonIgnore
    private Academy academy;

    // Teacher -> Students
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "teacher_student",
            joinColumns = @JoinColumn(name = "teacher_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id")
    )
    @JsonIgnore
    private Set<Member> teachingStudents = new HashSet<>();

    // Student -> Teachers (inverse)
    @Builder.Default
    @ManyToMany(mappedBy = "teachingStudents")
    @JsonIgnore
    private Set<Member> teachers = new HashSet<>();

    // Parent -> Children
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "parent_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id")
    )
    @JsonIgnore
    private Set<Member> parentStudents = new HashSet<>();

    // Child -> Parents (inverse)
    @Builder.Default
    @ManyToMany(mappedBy = "parentStudents")
    @JsonIgnore
    private Set<Member> parents = new HashSet<>();



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;


    @Builder
    private Member(Long id, String username, String password, String name,
                   String phone, Role role,
                   java.util.List<Member> teachingStudents, java.util.List<Member> teachers,
                   java.util.List<Member> parentStudents, java.util.List<Member> parents,
                   Academy academy, Lesson lesson) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        if (teachingStudents != null) teachingStudents.forEach(this::addStudent);
        if (teachers != null) teachers.forEach(teacher -> teacher.addStudent(this));
        if (parentStudents != null) parentStudents.forEach(this::addParentStudent);
        if (parents != null) parents.forEach(parent -> parent.addParentStudent(this));
        this.academy = academy;
        this.lesson = lesson;
    }

    // ===== 편의 메서드 (역할/무결성 검증 추가) =====

    public void addStudent(Member student) {
        requireRole(this, Role.TEACHER, "교사만 학생을 추가할 수 있습니다.");
        requireRole(student, Role.STUDENT, "추가 대상은 학생이어야 합니다.");
        if (isSelf(student)) return; // 자기 자신 무시
        if (teachingStudents.add(student)) {
            student.teachers.add(this);
        }
    }

    public void removeStudent(Member student) {
        if (teachingStudents.remove(student)) {
            student.teachers.remove(this);
        }
    }

    public void addParentStudent(Member child) {
        requireRole(this, Role.PARENT, "부모만 자녀를 추가할 수 있습니다.");
        requireRole(child, Role.STUDENT, "자녀 대상은 학생이어야 합니다.");
        if (isSelf(child)) return;
        if (parentStudents.add(child)) {
            child.parents.add(this);
        }
    }

    public void removeParentStudent(Member child) {
        if (parentStudents.remove(child)) {
            child.parents.remove(this);
        }
    }

    // ===== 내부 유틸 =====
    private boolean isSelf(Member other) {
        return this == other || (this.id != null && other.id != null && Objects.equals(this.id, other.id));
    }

    private void requireRole(Member m, Role expected, String message) {
        if (m.role != expected) {
            throw new IllegalArgumentException(message);
        }
    }

    // equals/hashCode: 영속 id 기반
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member m)) return false;
        return id != null && Objects.equals(id, m.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}