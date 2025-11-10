package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone,
        String name, // 로그인에선 미사용이면 제약 없음
        @NotBlank String password
) { }