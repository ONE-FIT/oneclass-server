package oneclass.oneclass.global.auth.academy.dto;

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
