package oneclass.oneclass.global.auth.academy.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Academy {
    @Id
    private String academyCode;

    private String academyName;
    private String password;
    private String checkPassword;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 학원에 속한 멤버들
    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @Builder
    public Academy(String academyCode, String academyName, String password, String email, Role role, List<Member> members, String checkPassword) {
        this.academyCode = academyCode;
        this.academyName = academyName;
        this.password = password;
        this.checkPassword = checkPassword;
        this.email = email;
        this.role = role;
        this.members = (members == null) ? new ArrayList<>() : members;
    }
}
