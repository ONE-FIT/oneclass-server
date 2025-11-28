package oneclass.oneclass.domain.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import oneclass.oneclass.domain.academy.entity.Academy;

@Getter
@AllArgsConstructor
public class PendingAcademyResponse {
    private String academyCode;
    private String academyName;
    private String email;
    private String phone;
    private Academy.Status status;
}