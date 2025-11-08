package oneclass.oneclass.domain.academy.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AcademySignupRequest {
    private String academyName;
    private String email;
    private String password;
    private String checkPassword;
    private String phone;
}
