package oneclass.oneclass.domain.sendon.event;

public record AnnounceForLessonSavedEvent(
        String content,
        String title,
        Long lessonId
) {
}
