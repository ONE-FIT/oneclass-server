package oneclass.oneclass.domain.lesson.dto.request;

import oneclass.oneclass.domain.member.entity.Member;

public record UpdateLessonRequest(
        Long lessonId,
        String title,
        Long teacherId,
        Member student
) {
}
