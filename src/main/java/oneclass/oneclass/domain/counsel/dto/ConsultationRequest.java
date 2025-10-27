package oneclass.oneclass.domain.counsel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationRequest {
    @NotBlank
    private String name;            // 학생이름

    @NotBlank
    @Pattern(regexp = "^[0-9\\-+]{9,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;           // 학생전번

    private String parentPhone;     // 부모전번

    // 사용자가 "2025-10-08 14:30" 같은 형식으로 보내면 자동 파싱
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime date;// 희망하는 날짜

    private String type;            // 신규 or 재학
    private String subject;         // 과목
    private String description;     // 내용

}