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
    private String username;//이름
    private String password;//비번
    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    // Academy와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_code")
    private Academy academy;
}