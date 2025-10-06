package oneclass.oneclass.domain.counsel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;

import java.time.LocalDateTime;

@Data
public class ChangeConsultationStatusRequest {
    // 식별은 가급적 id로! (중복 방지)
    private Long consultationId;

    // 과거 구조 유지가 필요하면 아래 두 필드로 fallback
    private String name;
    private String phone;

    private ConsultationStatus status; // 상담자가 정하는 상태(필요 시 null 허용하여 날짜만 변경 가능)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime date;
    private String subject;
    private String description;
}