package oneclass.oneclass.domain.member.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationCode {
    @Id
    private String phone;
    private String code;
    private LocalDateTime expiry;
}
