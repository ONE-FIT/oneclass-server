package oneclass.oneclass.global.auth.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.global.auth.academy.entity.Academy;

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
    private String username;//userId
    private String password;//비번

    @Column(nullable = false)
    private String name;//이름


    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @ManyToOne
    private Member teacher; // 학생, 선생님용

    @ManyToOne
    private Member student; // 부모용(자녀 Member 참조)

    // Academy와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_code", referencedColumnName = "academyCode")
    private Academy academy;
}