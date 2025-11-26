package oneclass.oneclass.domain.member.entity;


import jakarta.persistence.*;
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
        PHONE,
        EMAIL,
        ADMIN_EMAIL // 필요하면 추가
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String identifier; // 전화번호 또는 이메일, 혹은 기타 키

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Type type;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(nullable = false, length = 16)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @Column(nullable = false)
    private boolean used = false;
}
