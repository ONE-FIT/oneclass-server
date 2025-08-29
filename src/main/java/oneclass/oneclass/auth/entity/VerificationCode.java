package oneclass.oneclass.auth.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationCode {
    @Id
    private String usernameOrEmail;
    private String code;
    private Long expiry;
}
