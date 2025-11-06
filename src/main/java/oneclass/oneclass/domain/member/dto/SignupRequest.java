package oneclass.oneclass.domain.member.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.domain.member.entity.Role;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String checkPassword;
    private String name;
//    private String email;
    private String phone;
    private Role role;
    private String verificationCode;
    private String academyCode;
    private List<String> studentUsername = new ArrayList<>(); // studentId → studentUsername

    @Builder
    public SignupRequest(String username, String password, String name, String email, String phone,
                         Role role, String verificationCode, String academyCode, List<String> studentUsername) {
        this.username = username;
        this.password = password;
        this.checkPassword = password;
        this.name = name;
//        this.email = email;
        this.phone = phone;
        this.role = role;
        this.verificationCode = verificationCode;
        this.academyCode = academyCode;
        this.studentUsername = (studentUsername == null) ? new ArrayList<>() : studentUsername;
    }
}