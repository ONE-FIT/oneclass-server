package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * username 기반 로그인 요청 DTO
 * - 기존 phone 기반을 username 기반으로 전환
 */
public record LoginRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 8, max = 64) String password
) {}