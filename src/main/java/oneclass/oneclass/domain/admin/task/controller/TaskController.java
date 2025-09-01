package oneclass.oneclass.domain.admin.task.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.admin.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.domain.admin.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.admin.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.admin.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.admin.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins ="*")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    public TaskResponse createTask(@RequestParam CreateTaskRequest request) {
        ResponseEntity.ok(Map.of("message", "공지가 생성되었습니다."));
        return taskService.createTask(request);
    }

    @GetMapping("/{id}")
    public TaskResponse findTaskById(@PathVariable Long id) {
        ResponseEntity.ok(Map.of("message", "공지가 제공되었습니다."));
        return taskService.findTaskById(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse updateTask( @RequestBody UpdateTaskRequest request) {
        ResponseEntity.ok(Map.of("message", "공지가 수정되었습니다."));
        return taskService.updateTask(request);
    }

    @DeleteMapping("/delete")
    public void deleteTask(@RequestParam Long id) {
        ResponseEntity.ok(Map.of("message", "공지가 삭제되었습니다."));
        taskService.deleteTask(id);
    }

    @GetMapping()
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }
}
