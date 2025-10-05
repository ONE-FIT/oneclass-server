package oneclass.oneclass.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
    private String checkPassword;

    @JsonAlias({"code","verification_code","authCode","auth_code"})
    private String verificationCode;
}
