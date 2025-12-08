package oneclass.oneclass.domain.member.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "verification_code")
public class VerificationCode {

    public enum Type {
        EMAIL,
        ADMIN_EMAIL, // 필요하면 추가
        RESET_PASSWORD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true,length = 255)
    private String email; //이메일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Type type;

    @Column(nullable = true, length = 13)
    private String phone;

    @Column(nullable = false, length = 16)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @Column(nullable = false)
    private boolean used = false;
}
