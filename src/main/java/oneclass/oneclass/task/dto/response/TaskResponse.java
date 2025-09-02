package oneclass.oneclass.task.dto.response;

import oneclass.oneclass.task.entity.Task;

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
