package oneclass.oneclass.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseToken {
    private String accessToken;
    private String refreshToken;
}
