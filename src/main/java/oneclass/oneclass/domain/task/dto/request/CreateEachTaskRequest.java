package oneclass.oneclass.domain.task.dto.request;

import oneclass.oneclass.domain.member.entity.Member;

import java.time.LocalDate;

public record CreateEachTaskRequest(
        String title,
        String description,
        LocalDate dueDate,
        Member teacher,
        Member assignedBy
) {
}
