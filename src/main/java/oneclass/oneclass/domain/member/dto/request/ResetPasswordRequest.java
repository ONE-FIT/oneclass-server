package oneclass.oneclass.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import oneclass.oneclass.global.validation.PasswordMatches;

@PasswordMatches(password = "password", confirm = "checkPassword")
public record ResetPasswordRequest(
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone,
        @NotBlank @Size(min = 8, max = 64) String newPassword,
        @NotBlank @Size(min = 8, max = 64) String checkPassword,
        @JsonAlias({"code","verification_code","authCode","auth_code"}) @NotBlank String verificationCode) { }