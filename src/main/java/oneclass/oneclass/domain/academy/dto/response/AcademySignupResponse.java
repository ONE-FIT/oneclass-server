package oneclass.oneclass.domain.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AcademySignupResponse {
    private String academyCode;
    private String academyName;
    private String email;
    private String phone;
}