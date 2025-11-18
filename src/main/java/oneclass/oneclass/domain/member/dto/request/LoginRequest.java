package oneclass.oneclass.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 로그인 요청 DTO (username 기반)
 * - backward-compatible: "phone" 키로도 들어오면 username에 매핑되도록 @JsonAlias 사용
 * - 컨트롤러/서비스에서 req.username()/req.password() 또는 req.getUsername()/req.getPassword()로 사용 가능합니다.
 */
public record LoginRequest(
        @NotBlank @Size(min = 3, max = 100)
        @JsonAlias({"username", "phone"})
        String username,

        @NotBlank @Size(min = 8, max = 64)
        String password
) {
    // 기존 코드에서 getUsername()/getPassword() 호출이 남아있는 경우를 대비한 브릿지 getter
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}