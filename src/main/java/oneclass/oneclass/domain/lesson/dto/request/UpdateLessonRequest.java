package oneclass.oneclass.domain.lesson.dto.request;


public record UpdateLessonRequest(
        Long lessonId,
        String title,
        Long teacherId
) {
}
