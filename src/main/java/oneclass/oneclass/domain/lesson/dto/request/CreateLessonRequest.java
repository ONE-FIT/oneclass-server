package oneclass.oneclass.domain.lesson.dto.request;

import oneclass.oneclass.global.auth.member.entity.Member;

public record CreateLessonRequest(
        String title,
        Member teacher
) {
}
