package oneclass.oneclass.domain.task.dto.request;


import oneclass.oneclass.domain.lesson.entity.Lesson;

import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        LocalDate dueDate,
        Lesson lid
) {
}
