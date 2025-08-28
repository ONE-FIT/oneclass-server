package oneclass.oneclass.auth.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.auth.entity.Role;

@Data
@NoArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private Role role;
}
