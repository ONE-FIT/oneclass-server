package oneclass.oneclass.domain.member.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.domain.member.entity.Role;

import java.util.List;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String password;
    private String checkPassword;
    private String name;
//    private String email;
    private String phone;
    private Role role;
    private String verificationCode;
    private String academyCode;
    private String studentPhone;

    @Builder
    public SignupRequest(String password, String name, String email, String phone,
                         Role role, String verificationCode, String academyCode, List<String> studentUsername) {
        this.password = password;
        this.checkPassword = password;
        this.name = name;
//        this.email = email;
        this.phone = phone;
        this.role = role;
        this.verificationCode = verificationCode;
        this.academyCode = academyCode;
    }
}