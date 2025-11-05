package oneclass.oneclass.domain.member.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TeacherStudentsRequest {
    @NotEmpty(message = "학생 전화번호 목록이 필요합니다.")
    private List<String> phone;
    private String password;
}