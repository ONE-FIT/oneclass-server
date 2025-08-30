package oneclass.oneclass.domain.auth.member.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.domain.auth.member.entity.Role;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private Role role;
}
