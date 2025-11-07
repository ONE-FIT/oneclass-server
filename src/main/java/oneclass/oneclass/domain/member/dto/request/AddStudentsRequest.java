package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AddStudentsRequest {
    @NotBlank
    private String username; // 부모 username
    @NotBlank
    private String password; // 부모 비밀번호 확인
    @NotEmpty
    private List<String> studentUsernames; // 자녀 usernames
}