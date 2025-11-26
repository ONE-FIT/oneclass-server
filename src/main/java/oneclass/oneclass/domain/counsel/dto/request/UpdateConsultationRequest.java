package oneclass.oneclass.domain.counsel.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record UpdateConsultationRequest(
        @Positive(message = "유효하지 않은 상담 ID입니다.")
        Long consultationId,
        
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name,
        
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        @NotNull(message = "전화번호는 필수입니다.")
        String phone,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime date,
        
        @Size(max = 100, message = "상담 주제는 100자를 초과할 수 없습니다.")
        String subject,
        
        @Size(max = 1000, message = "상담 설명은 1000자를 초과할 수 없습니다.")
        String description
) { }
