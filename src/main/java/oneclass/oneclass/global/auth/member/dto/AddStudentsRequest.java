package oneclass.oneclass.global.auth.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AddStudentsRequest {
    private String username;
    private String password;
    private List<Long> studentId = new ArrayList<>(); // 혹은 Long 타입

    // 필요하면 @Builder 생성자 추가
}