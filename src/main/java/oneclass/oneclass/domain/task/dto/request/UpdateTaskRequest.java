package oneclass.oneclass.domain.task.dto.request;

import java.time.LocalDate;

public record UpdateTaskRequest(
        Long id,
        String description,
        LocalDate dueDate
) {
}
