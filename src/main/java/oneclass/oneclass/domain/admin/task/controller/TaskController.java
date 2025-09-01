package oneclass.oneclass.domain.admin.task.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.admin.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.admin.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.admin.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.admin.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins ="*")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    public void createTask(@RequestParam CreateTaskRequest request) {
        taskService.createTask(request);
    }

    @GetMapping("/{id}")
    public TaskResponse findTaskById(@PathVariable Long id) {
        return taskService.findTaskById(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse updateTask( @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(request);
    }

    @DeleteMapping("/delete")
    public void deleteTask(@RequestParam Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping()
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }
}
