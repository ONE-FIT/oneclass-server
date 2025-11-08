package oneclass.oneclass.domain.member.dto.request;


public record LoginRequest(
        String phone,
        String name,
        String password
) {
}
