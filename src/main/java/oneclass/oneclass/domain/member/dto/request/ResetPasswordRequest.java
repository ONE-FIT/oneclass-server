package oneclass.oneclass.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import oneclass.oneclass.global.validation.PasswordMatches;

@PasswordMatches(password = "newPassword", confirm = "checkPassword")
public record ResetPasswordRequest(
        @NotBlank String phone,
        @NotBlank @Size(min = 8, max = 64) String newPassword,
        @NotBlank @Size(min = 8, max = 64) String checkPassword,
        @JsonAlias({"code","verification_code","authCode","auth_code"}) String verificationCode) { }