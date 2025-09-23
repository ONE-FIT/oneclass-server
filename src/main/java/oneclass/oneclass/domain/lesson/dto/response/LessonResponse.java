package oneclass.oneclass.domain.lesson.dto.response;



import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.global.auth.member.entity.Member;

public record LessonResponse(
        Long lid,
        String title,
        Member teacher
) {
    public static LessonResponse of(Lesson lesson) {
        return new LessonResponse(
                lesson.getLid(),
                lesson.getTitle(),
                lesson.getTeacher()
        );
    }
}
