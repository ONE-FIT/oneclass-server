package oneclass.oneclass.domain.counsel.dto.response;

import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;

public record ConsultationDetailResponse(
        String name,
        String phone,
        String type,
        String subject,
        String description,
        ConsultationStatus status
) {
    public static ConsultationDetailResponse from(Consultation consultation) {
        return new ConsultationDetailResponse(
                consultation.getName(),
                consultation.getPhone(),
                consultation.getType(),
                consultation.getSubject(),
                consultation.getDescription(),
                consultation.getStatus()
        );
    }
}