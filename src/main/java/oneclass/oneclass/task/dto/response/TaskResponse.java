package oneclass.oneclass.task.dto.response;

import oneclass.oneclass.auth.member.entity.Member;
import oneclass.oneclass.task.entity.Task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Member AssignedTo,
        LocalDate dueDate
) {
    public static TaskResponse of (Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getDueDate()
        );
    }
}
