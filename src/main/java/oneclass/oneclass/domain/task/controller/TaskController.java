package oneclass.oneclass.domain.task.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@CrossOrigin(origins ="*")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    public TaskResponse createTask(@RequestBody CreateTaskRequest request) {
        ResponseEntity.ok(Map.of("message", "과제가 생성되었습니다."));
        return taskService.createTask(request);
    }

    @GetMapping("/{id}")
    public TaskResponse findTaskById(@PathVariable Long id) {
        ResponseEntity.ok(Map.of("message", "과제가 제공되었습니다."));
        return taskService.findTaskById(id);
    }
    @GetMapping("/{title}")
    public TaskResponse findTaskByTitle(@PathVariable String title) {
        ResponseEntity.ok(Map.of("message", "과제가 제공되었습니다."));
        return taskService.findTaskByTitle(title);
    }

    @PatchMapping("/{id}")
    public TaskResponse updateTask( @RequestBody UpdateTaskRequest request) {
        ResponseEntity.ok(Map.of("message", "과제가 수정되었습니다."));
        return taskService.updateTask(request);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        ResponseEntity.ok(Map.of("message", "과제가 삭제되었습니다."));
        taskService.deleteTask(id);
    }

    @GetMapping()
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

}
