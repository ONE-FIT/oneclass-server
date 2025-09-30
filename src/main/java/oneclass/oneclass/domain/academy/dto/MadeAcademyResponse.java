package oneclass.oneclass.domain.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MadeAcademyResponse {
    private String academyCode;
    private String academyName;
    private String email;
    private String phone;
}