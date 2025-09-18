package oneclass.oneclass.global.auth.member.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.global.auth.member.entity.Role;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private Role role;
    private String verificationCode;//인증코드(선생님 가입용)
    private String academyCode;
    private String studentId;//부모님 가입용
}
