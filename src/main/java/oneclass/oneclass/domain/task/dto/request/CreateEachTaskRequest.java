package oneclass.oneclass.domain.task.dto.request;

import java.time.LocalDate;

public record CreateEachTaskRequest(
        String title,
        String description,
        LocalDate dueDate,
        Long teacherId,
        Long studentId
) {
}
