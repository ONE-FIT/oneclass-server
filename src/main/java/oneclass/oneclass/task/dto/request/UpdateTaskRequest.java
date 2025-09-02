package oneclass.oneclass.task.dto.request;

import oneclass.oneclass.auth.member.entity.Member;

import java.time.LocalDate;

public record UpdateTaskRequest(
        Long id,
        String description,
        Member assignedTo,
        LocalDate dueDate
) {
}
