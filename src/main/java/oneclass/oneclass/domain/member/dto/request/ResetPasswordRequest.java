package oneclass.oneclass.domain.member.dto.request;


public record ResetPasswordRequest(
        String phone,
        String newPassword,
        String checkPassword,
        String verificationCode
) {
}
