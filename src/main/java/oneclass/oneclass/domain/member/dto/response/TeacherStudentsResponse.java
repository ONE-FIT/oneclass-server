package oneclass.oneclass.domain.member.dto.response;


import java.util.List;

public record TeacherStudentsResponse(
        MemberDto teacher,
        List<MemberDto> students
) {
}