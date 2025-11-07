package oneclass.oneclass.domain.lesson.dto.response;

import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.member.entity.Member;

public record LessonResponse(
        Long lessonId,
        String title,
        Member teacher
) {
    public static LessonResponse of(Lesson lesson) {
        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getTeacher()
        );
    }
}
