package oneclass.oneclass.domain.member.dto.response;

public record ResponseToken(
        String accessToken,
        String refreshToken
) { }