package oneclass.oneclass.domain.admin.task.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.admin.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.admin.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.admin.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.admin.task.entity.Task;
import oneclass.oneclass.domain.admin.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public void createTask(CreateTaskRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .build();
        taskRepository.save(task);
    }

    public TaskResponse findTaskById(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            throw new IllegalArgumentException(id + "는 존재하지 않습니다");
        }

        return TaskResponse.of(task);
    }


    public  TaskResponse updateTask(UpdateTaskRequest request) {
        Task task = taskRepository.findById(request.id()).orElse(null);
        if (task == null) {
            throw new IllegalArgumentException(request.id()+"는 없는 과제 입니다.");
        }
        task.setDescription(request.description());

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
