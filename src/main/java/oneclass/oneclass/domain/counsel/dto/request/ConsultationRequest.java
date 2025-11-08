package oneclass.oneclass.domain.counsel.dto.request;


import java.time.LocalDateTime;

public record ConsultationRequest(
        String name,
        String phone,
        String parentPhone,
        LocalDateTime date,
        String type,
        String subject,
        String description) {
}