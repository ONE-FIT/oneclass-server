package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSignupRequest(
        @NotBlank @Size(min = 1, max = 100) String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 8, max = 64) String checkPassword,
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String email,
        String verificationCode
) {
}
