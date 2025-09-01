package oneclass.oneclass.domain.admin.task.dto.response;

import oneclass.oneclass.domain.admin.task.entity.Task;

public record TaskResponse(
        Long id,
        String title,
        String description
) {
    public static TaskResponse of (Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription()
        );
    }
}
