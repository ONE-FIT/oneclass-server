package oneclass.oneclass.domain.counsel.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;

import java.time.LocalDateTime;

public record ChangeConsultationStatusRequest(
        Long consultationId,
        String name, // ID가 없을 때 name+phone으로 조회하므로 NotBlank는 걸지 않음
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        String phone, // null 허용(조건부), 값이 있으면 형식 검증
        ConsultationStatus status, // 전이 대상(옵션)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul") LocalDateTime date,        // 옵션 업데이트 필드
        String subject,            // 옵션 업데이트 필드
        String description         // 옵션 업데이트 필드
) { }