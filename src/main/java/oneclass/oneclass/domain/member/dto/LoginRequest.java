package oneclass.oneclass.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String name; // 닉네임 또는 이름으로 로그인
    private String password;
}
