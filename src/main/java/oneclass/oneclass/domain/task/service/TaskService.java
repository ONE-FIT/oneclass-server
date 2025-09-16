package oneclass.oneclass.domain.task.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .build();
        return TaskResponse.of(taskRepository.save(task));
        // 리턴시 assignedTo 에게 메세지 발송
    }

    public TaskResponse findTaskById(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            throw new IllegalArgumentException(id + "는 존재하지 않습니다");
        }

        return TaskResponse.of(task);
    }

    public TaskResponse findTaskByTitle(String title) {
        Task task = taskRepository.findByTitle(title).orElse(null);
        if (task == null) {
            throw new IllegalArgumentException(title + "는 존재하지 않습니다");
        }

        return TaskResponse.of(task);
    }


    public TaskResponse updateTask(UpdateTaskRequest request) {
        Task task = taskRepository.findById(request.id()).orElse(null);
        if (task == null) {
            throw new IllegalArgumentException(request.id()+"는 없는 과제 입니다.");
        }
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        taskRepository.save(task);

        return TaskResponse.of(task);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            throw new IllegalArgumentException(id+"는 없는 과제 입니다.");
        }
        taskRepository.delete(task);

    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream().map(TaskResponse::of).collect(Collectors.toList());
    }
}
