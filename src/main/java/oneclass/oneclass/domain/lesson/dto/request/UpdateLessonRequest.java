package oneclass.oneclass.domain.lesson.dto.request;

import oneclass.oneclass.global.auth.member.entity.Member;

public record UpdateLessonRequest(
        Long lid,
        String title,
        Member teacher,
        Member student
) {
}
