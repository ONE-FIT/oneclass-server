package oneclass.oneclass.domain.academy.entity;

import jakarta.persistence.*;
import lombok.*;
import oneclass.oneclass.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 프록시용
@Getter
@Entity
@Setter
public class Academy {
    @Id
    private String academyCode;

    private String academyName;
    private String password;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @Builder
    public Academy(String academyCode, String academyName, String password, String email, String phone, Role role, List<Member> members) {
        this.academyCode = academyCode;
        this.academyName = academyName;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        if (members != null) {
            this.members = members;
        }
    }
}