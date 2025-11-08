package oneclass.oneclass.domain.academy.dto.request;


public record ResetAcademyPasswordRequest(
        String academyCode,
        String academyName,
        String verificationCode,
        String newPassword,
        String checkPassword) {
}
