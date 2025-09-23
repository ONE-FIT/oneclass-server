package oneclass.oneclass.domain.task.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@CrossOrigin(origins ="*")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    public TaskResponse createTask(@RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping("/id/{id}")
    public TaskResponse findTaskById(@PathVariable Long id) { //우리가 잠시 사용할 기능
        return taskService.findTaskById(id);
    }

    @GetMapping("/title/{title}")
    public TaskResponse findTaskByTitle(@PathVariable String title) { //유저가 사용할 기능
        return taskService.findTaskByTitle(title);
    }

    @PatchMapping()
    public TaskResponse updateTask(@RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(request);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/all")
    public List<TaskResponse> findAllTask() {
        return taskService.findAll();
    }

}
