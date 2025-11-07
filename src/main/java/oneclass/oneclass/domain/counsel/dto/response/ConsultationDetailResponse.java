package oneclass.oneclass.domain.counsel.dto.response;

import lombok.Data;
import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;

@Data
public class ConsultationDetailResponse {
    private String name;
    private String phone;
    private String type;
    private String subject;
    private String description;
    private ConsultationStatus status;

    public static ConsultationDetailResponse from(Consultation consultation) {
        ConsultationDetailResponse res = new ConsultationDetailResponse();
        res.setName(consultation.getName());
        res.setPhone(consultation.getPhone());
        res.setType(consultation.getType());
        res.setSubject(consultation.getSubject());
        res.setDescription(consultation.getDescription());
        res.setStatus(consultation.getStatus());
        return res;
    }
}