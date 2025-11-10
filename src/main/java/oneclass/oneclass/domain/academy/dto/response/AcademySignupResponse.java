package oneclass.oneclass.domain.academy.dto.response;

public record AcademySignupResponse(
        String academyCode,
        String academyName,
        String email,
        String phone
) { }