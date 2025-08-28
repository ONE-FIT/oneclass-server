package oneclass.oneclass.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindUsernameRequest {
    private String emailOrPhone;
}
