package oneclass.oneclass.domain.auth.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 이름
    private String password; // 비번

    private String phone;
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role; // STUDENT / TEACHER / PARENT
}

