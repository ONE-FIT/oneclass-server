package oneclass.oneclass.domain.lesson.dto.request;

public record CreateLessonRequest(
        String title,
        Long teacherId
) {
}
