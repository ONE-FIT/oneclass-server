package oneclass.oneclass.domain.lesson.dto.request;

import oneclass.oneclass.domain.member.entity.Member;

public record CreateLessonRequest(
        String title,
        Member teacher
) {
}
