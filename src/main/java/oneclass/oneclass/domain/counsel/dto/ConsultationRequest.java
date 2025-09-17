package oneclass.oneclass.domain.counsel.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class ConsultationRequest {
    private  String name;
    private String phone;
    private String type;
    private String subject;
    private String description;
}