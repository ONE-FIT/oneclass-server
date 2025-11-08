package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record TeacherStudentsRequest(
        @NotEmpty List<@Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String> studentPhones,
        String password // 추가 API(삭제)에서 비밀번호가 필요 없으므로 강제하지 않음(값이 있으면 별도 서비스에서 검증)
) {
    public List<String> studentPhones() {
        return studentPhones;
    }
}