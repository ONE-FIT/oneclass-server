package oneclass.oneclass.domain.task.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.domain.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTaskService {

    private final TaskRepository taskRepository;

    /**
     * 특정 taskId와 상태에 맞는 학생(Member) 리스트 조회
     */
    public List<Member> findMemberByTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 Task가 존재하지 않습니다: " + taskId));

        // TaskAssignment에서 상태 맞는 학생만 필터링
        return task.getAssignments().stream()
                .filter(assignment -> assignment.getTaskStatus() == status)
                .map(TaskAssignment::getStudent)
                .collect(Collectors.toList());
    }
}