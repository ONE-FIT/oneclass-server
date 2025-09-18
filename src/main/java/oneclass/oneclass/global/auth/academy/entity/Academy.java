package oneclass.oneclass.global.auth.academy.entity;

import jakarta.persistence.*;
import lombok.*;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Academy {
    @Id
    private String academyCode;

    private String academyName;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 학원에 속한 멤버들
    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();
}