package oneclass.oneclass.domain.academy.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import oneclass.oneclass.global.validation.PasswordMatches;

@PasswordMatches(password = "password", confirm = "checkPassword")
public record AcademySignupRequest(
        @NotBlank String academyName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 8, max = 64) String checkPassword,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone
) { }