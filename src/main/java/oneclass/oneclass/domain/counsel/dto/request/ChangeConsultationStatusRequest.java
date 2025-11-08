package oneclass.oneclass.domain.counsel.dto.request;


import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;
import java.time.LocalDateTime;

public record ChangeConsultationStatusRequest(
        Long consultationId,
        String name,
        String phone,
        ConsultationStatus status,
        LocalDateTime date,
        String subject,
        String description) {
}