package oneclass.oneclass.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import oneclass.oneclass.domain.academy.entity.Academy;

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

    @Column(unique = true, length = 100)
    private String username;

    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
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
    private Set<Member> teachingStudents = new HashSet<>();

    // Student -> Teachers (inverse)
    @Builder.Default
    @ManyToMany(mappedBy = "teachingStudents")
    private Set<Member> teachers = new HashSet<>();

    // Parent -> Children
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "parent_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id")
    )
    private Set<Member> parentStudents = new HashSet<>();

    // Child -> Parents (inverse)
    @Builder.Default
    @ManyToMany(mappedBy = "parentStudents")
    private Set<Member> parents = new HashSet<>();

    // 명시적 getter들(IDE Lombok 문제 회피용)
    public Set<Member> getTeachers() { return teachers; }
    public Set<Member> getTeachingStudents() { return teachingStudents; }
    public Set<Member> getParentStudents() { return parentStudents; }
    public Set<Member> getParents() { return parents; }

    // 편의 메서드(양방향 동기화 + 중복 방지)
    public void addStudent(Member student) {
        if (this.teachingStudents == null) this.teachingStudents = new HashSet<>();
        if (student.teachers == null) student.teachers = new HashSet<>();
        if (this.teachingStudents.add(student)) {
            student.teachers.add(this);
        }
    }
    public void removeStudent(Member student) {
        if (this.teachingStudents != null && this.teachingStudents.remove(student)) {
            if (student.teachers != null) student.teachers.remove(this);
        }
    }
    public void addParentStudent(Member child) {
        if (this.parentStudents == null) this.parentStudents = new HashSet<>();
        if (child.parents == null) child.parents = new HashSet<>();
        if (this.parentStudents.add(child)) {
            child.parents.add(this);
        }
    }
    public void removeParentStudent(Member child) {
        if (this.parentStudents != null && this.parentStudents.remove(child)) {
            if (child.parents != null) child.parents.remove(this);
        }
    }

    // Set 안정성: id 기반 equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member m)) return false;
        return id != null && Objects.equals(id, m.id);
    }
    @Override
    public int hashCode() { return 31; }
}