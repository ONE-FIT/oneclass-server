package oneclass.oneclass.domain.counsel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record ConsultationRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String parentPhone,
        LocalDateTime date, // 희망 날짜(선택)
        @NotBlank String type,
        String subject,     // 선택
        String description  // 선택
) { }