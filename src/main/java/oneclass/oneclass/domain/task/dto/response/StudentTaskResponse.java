package oneclass.oneclass.domain.task.dto.response;

import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;

import java.time.LocalDate;

public record StudentTaskResponse(
        Long assignmentId,     // 할당 기록 ID
        Long taskId,           // 과제 원본 ID
        String title,          // 과제 제목
        String description,    // 과제 설명
        LocalDate dueDate,     // 마감일
        TaskStatus status      // 본인의 진행 상태 (ASSIGNED, SUBMITTED 등)
) {
    public static StudentTaskResponse from(TaskAssignment assignment) {
        return new StudentTaskResponse(
                assignment.getId(),
                assignment.getTask().getId(),
                assignment.getTask().getTitle(),
                assignment.getTask().getDescription(),
                assignment.getTask().getDueDate(),
                assignment.getTaskStatus()
        );
    }
}