package oneclass.oneclass.domain.task.dto.response;

import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.task.entity.Task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        Lesson Lid
) {
    public static TaskResponse of (Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getLesson()
        );
    }
}