package oneclass.oneclass.domain.task.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.sms.event.TaskSavedEvent;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.error.TaskError;
import oneclass.oneclass.domain.task.repository.TaskRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .build();

        Task savedTask = taskRepository.save(task);

        // TODO: 메세지의 내용을 팀원과 토론하세요.
        eventPublisher.publishEvent(new TaskSavedEvent(request.description(), request.title()));

        return TaskResponse.of(savedTask);
        // 리턴시 assignedTo 에게 메세지 발송
    }

    public TaskResponse findTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
        return TaskResponse.of(task);
    }

    public TaskResponse findTaskByTitle(String title) {
        Task task = taskRepository.findByTitle(title)
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
        return TaskResponse.of(task);
    }


    public TaskResponse updateTask(UpdateTaskRequest request) {
        Task task = taskRepository.findById(request.id())
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        taskRepository.save(task);

        return TaskResponse.of(task);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->new CustomException(TaskError.NOT_FOUND));
        taskRepository.delete(task);

    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream().map(TaskResponse::of).collect(Collectors.toList());
    }
}
