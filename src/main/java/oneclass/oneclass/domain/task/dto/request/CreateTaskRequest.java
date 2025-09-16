package oneclass.oneclass.domain.task.dto.request;

import oneclass.oneclass.global.member.entity.Member;

import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        Member assignedTo,
        LocalDate dueDate
) {
}
