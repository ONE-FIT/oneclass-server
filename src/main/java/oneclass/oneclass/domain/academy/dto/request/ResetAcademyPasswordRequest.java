package oneclass.oneclass.domain.academy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import oneclass.oneclass.global.validation.PasswordMatches;

@PasswordMatches(password = "password", confirm = "checkPassword")
public record ResetAcademyPasswordRequest(
        @NotBlank String academyCode,
        @NotBlank String academyName,
        @NotBlank String verificationCode,
        @NotBlank @Size(min = 8, max = 64) String newPassword,
        @NotBlank @Size(min = 8, max = 64) String checkPassword
) { }