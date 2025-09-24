package oneclass.oneclass.global.auth.academy.entity;

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
public class AcademyVerificationCode {
    @Id
    private String academyCode;
    private String code;
    private LocalDateTime expiry;
}
