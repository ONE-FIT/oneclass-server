package oneclass.oneclass.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindUsernameRequest {
    private String emailOrPhone;
}
