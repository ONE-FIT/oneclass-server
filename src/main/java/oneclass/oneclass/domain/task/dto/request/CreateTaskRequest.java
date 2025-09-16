package oneclass.oneclass.domain.task.dto.request;


import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        LocalDate dueDate
) {
}
