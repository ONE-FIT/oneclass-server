package oneclass.oneclass.counsel.dto;

import lombok.Data;

@Data
public class ConsultationDetailResponse {
    private String name;
    private String phone;
    private String type;
    private String subject;
    private String description;
    private String status;
    private String scheduleTime;

    // 생성자 혹은 static from(Entity) 메서드
    public static ConsultationDetailResponse from(oneclass.oneclass.counsel.entity.Consultation consultation) {
        ConsultationDetailResponse res = new ConsultationDetailResponse();
        res.setName(consultation.getName());
        res.setPhone(consultation.getPhone());
        res.setType(consultation.getType());
        res.setSubject(consultation.getSubject());
        res.setDescription(consultation.getDescription());
        res.setStatus(consultation.getStatus());
        // 필요하다면 scheduleTime 등도 추가
        return res;
    }
}