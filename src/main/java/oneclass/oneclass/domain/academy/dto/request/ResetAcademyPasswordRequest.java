package oneclass.oneclass.domain.academy.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetAcademyPasswordRequest {
    private String academyCode;
    private String academyName;
    private String verificationCode;
    private String newPassword;
    private String checkPassword;
}
