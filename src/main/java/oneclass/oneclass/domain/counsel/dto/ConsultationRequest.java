package oneclass.oneclass.domain.counsel.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Data
@Validated
public class ConsultationRequest {
    private  String name;//학생이름
    private String phone;//학생전번
    private String parentPhone;//부모전번
    private LocalDateTime date;//희망하는 날짜
    private String type;//신규 or 재학
    private String subject;//과목
    private String description;//내용
    private String status;//상담 신청/확정됨 표시

}