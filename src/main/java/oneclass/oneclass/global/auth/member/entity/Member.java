package oneclass.oneclass.global.auth.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;//이름
    private String password;//비번
    private String phone;
    private String email;

    private String academyCode;//선생님 회원가입용

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;
}
