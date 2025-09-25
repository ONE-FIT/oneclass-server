package oneclass.oneclass.global.auth.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import oneclass.oneclass.global.auth.member.entity.Role;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
