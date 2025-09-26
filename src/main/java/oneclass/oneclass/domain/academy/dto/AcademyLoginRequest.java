package oneclass.oneclass.domain.academy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class AcademyLoginRequest {
    private String academyCode;
    private String academyName;
    private String password;
}
