package oneclass.oneclass.domain.task.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.response.MemberTaskResponse;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.error.TaskError;
import oneclass.oneclass.domain.task.repository.TaskRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTaskService {

    private final TaskRepository taskRepository;

    /**
     * 특정 taskId와 상태에 맞는 학생(MemberDto) 리스트 조회
     */
    public List<MemberTaskResponse> findMemberByTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));

        return task.getAssignments().stream()
                .filter(assignment -> assignment.getTaskStatus() == status)
                .map(TaskAssignment::getStudent)
                .map(MemberTaskResponse::fromEntity) // DTO로 변환
                .collect(Collectors.toList());
    }
}
