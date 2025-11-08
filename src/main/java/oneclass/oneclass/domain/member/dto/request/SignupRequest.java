package oneclass.oneclass.domain.member.dto.request;

import oneclass.oneclass.domain.member.entity.Role;

public record SignupRequest(
        String password,
        String checkPassword,
        String name,
        String phone,
        Role role,
        String verificationCode,
        String academyCode,
        String studentPhone
) {
}