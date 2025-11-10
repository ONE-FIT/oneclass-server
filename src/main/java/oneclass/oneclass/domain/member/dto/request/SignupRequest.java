package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import oneclass.oneclass.domain.member.entity.Role;
import oneclass.oneclass.global.validation.PasswordMatches;

@PasswordMatches(password = "password", confirm = "checkPassword")
public record SignupRequest(
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 8, max = 64) String checkPassword,
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone,
        @NotNull Role role,
        // 아래 3개는 역할에 따라 선택(값이 있을 때만 형식 검증)
        String verificationCode,
        String academyCode,
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        String studentPhone
) { }