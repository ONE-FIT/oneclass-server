package oneclass.oneclass.domain.auth.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
    private String verificationCode;
}
