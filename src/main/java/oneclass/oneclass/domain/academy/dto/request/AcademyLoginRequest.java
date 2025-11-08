package oneclass.oneclass.domain.academy.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcademyLoginRequest(
        @NotBlank String academyCode,
        @NotBlank String academyName,
        @NotBlank String password
) { }