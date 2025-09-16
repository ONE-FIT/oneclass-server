package oneclass.oneclass.domain.task.dto.response;

import oneclass.oneclass.global.member.entity.Member;
import oneclass.oneclass.domain.task.entity.Task;

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
