package oneclass.oneclass.domain.counsel.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import oneclass.oneclass.domain.counsel.entity.Gender;

import java.time.LocalDateTime;

public record ConsultationRequest(
        @NotBlank(message = "상담 제목은 필수입니다.")
        @Size(max = 100, message = "상담 제목은 100자를 초과할 수 없습니다.")
        String title,
        
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name,
        
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        String phone,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime date,
        
        @NotBlank(message = "상담 유형은 필수입니다.")
        @Size(max = 50, message = "상담 유형은 50자를 초과할 수 없습니다.")
        String type,

        String subject,
        
        @Size(max = 1000, message = "상담 설명은 1000자를 초과할 수 없습니다.")
        String description,
        
        @Min(value = 0, message = "나이는 0~150 사이여야 합니다.")
        @Max(value = 150, message = "나이는 0~150 사이여야 합니다.")
        int age,
        
        Gender gender
) { }
