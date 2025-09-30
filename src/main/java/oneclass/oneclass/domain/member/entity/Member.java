package oneclass.oneclass.domain.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oneclass.oneclass.domain.academy.entity.Academy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 이름
    private String password; // 비번

    @Column(nullable = false)
    private String name;//이름

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @ManyToOne
    private Member teacher; // 선생님용

    @OneToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "parent_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Member> students = new ArrayList<>(); // ★ 기본값으로 초기화

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_code", referencedColumnName = "academyCode")
    private Academy academy;

    @Builder
    public Member(Long id, String username, String password, String name, String phone, String email, Role role,
                  Member teacher, List<Member> students, Academy academy) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.teacher = teacher;
        this.students = (students == null) ? new ArrayList<>() : students; // ★ null 체크
        this.academy = academy;
    }
}