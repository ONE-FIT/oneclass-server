package oneclass.oneclass.domain.auth.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindUsernameRequest {
    private String emailOrPhone;
}
