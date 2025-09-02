package oneclass.oneclass.task.dto.request;

import oneclass.oneclass.auth.member.entity.Member;

import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        Member assignedTo,
        LocalDate dueDate
) {
}
