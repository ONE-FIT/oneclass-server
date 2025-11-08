package oneclass.oneclass.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeacherStudentsResponse {
    private MemberDto teacher;
    private List<MemberDto> students;
}