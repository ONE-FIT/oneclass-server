package oneclass.oneclass.domain.task.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.request.CreateEachTaskRequest;
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

    @PostMapping("/create/{lessonId}")
    public TaskResponse createLessonTask(@RequestBody CreateTaskRequest request, Long lessonId) {
        return taskService.createLessonTask(request,lessonId);
    }

    @PostMapping("/createOne")
    public TaskResponse createEachTask(@RequestBody CreateEachTaskRequest request) {
        return taskService.createEachTask(request);

    }

    @GetMapping("/id/{id}")
    public TaskResponse findTaskById(@PathVariable Long id) { //우리가 잠시 사용할 기능
        return taskService.findTaskById(id);
    }

    @GetMapping("/title/{title}")
    public TaskResponse findTaskByTitle(@PathVariable String title) { //유저가 사용할 기능
        return taskService.findTaskByTitle(title);
    }

//    @GetMapping("/status/{status}")
//    public TaskResponse findTaskByStatus(@PathVariable TaskStatus status) {
//        return taskService.findTaskByStatus(status);
//    }

    @PatchMapping()
    public TaskResponse updateTask(@RequestBody UpdateTaskRequest request) { return taskService.updateTask(request); }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/all")
    public List<TaskResponse> findAllTask() {
        return taskService.findAll();
    }

}