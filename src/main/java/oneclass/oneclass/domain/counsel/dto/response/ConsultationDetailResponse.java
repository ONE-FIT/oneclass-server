package oneclass.oneclass.domain.counsel.dto.response;

import oneclass.oneclass.domain.counsel.entity.Consultation;

public record ConsultationDetailResponse(
        String name,
        String phone,
        String type,
        String subject,
        String description
) {
    public static ConsultationDetailResponse from(Consultation consultation) {
        return new ConsultationDetailResponse(
                consultation.getName(),
                consultation.getPhone(),
                consultation.getType(),
                consultation.getSubject(),
                consultation.getDescription()
        );
    }
}