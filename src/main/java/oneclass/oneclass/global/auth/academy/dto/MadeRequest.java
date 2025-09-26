package oneclass.oneclass.global.auth.academy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MadeRequest {
    private String academyName;
    private String email;
    private String password;
    private String checkPassword;
}
