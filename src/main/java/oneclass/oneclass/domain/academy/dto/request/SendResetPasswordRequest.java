package oneclass.oneclass.domain.academy.dto.request;

public record SendResetPasswordRequest(
        String academyCode,
        String academyName
) {
}
