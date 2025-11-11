package oneclass.oneclass.domain.counsel.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record ConsultationRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String parentPhone,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul") LocalDateTime date, // 희망 날짜(선택)
        @NotBlank String type,
        String subject,
        String description,
        String age,
        String gender
) { }