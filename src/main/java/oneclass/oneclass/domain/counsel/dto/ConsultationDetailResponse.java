package oneclass.oneclass.domain.counsel.dto;

import lombok.Data;
import oneclass.oneclass.domain.counsel.entity.Consultation;

@Data
public class ConsultationDetailResponse {
    private String name;
    private String phone;
    private String type;
    private String subject;
    private String description;
    private String status;
    private String scheduleTime;

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