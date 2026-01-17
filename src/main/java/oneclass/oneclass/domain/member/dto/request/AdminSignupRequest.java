package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminSignupRequest(
        @NotNull @Size(min = 1, max = 100) String username,
        @NotNull @Size(min = 8, max = 64) String password,
        @NotNull @Size(min = 8, max = 64) String checkPassword,
        @NotNull String name,
        @NotNull String phone,
        @NotNull String email,
        String verificationCode
) {
}
