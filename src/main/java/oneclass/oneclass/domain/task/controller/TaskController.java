package oneclass.oneclass.domain.task.controller;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "과제 생성",
            description = "과제를 레슨에 있는 모두에게 생성합니다.")
    public TaskResponse createLessonTask(@RequestBody CreateTaskRequest request,@PathVariable Long lessonId) {
        return taskService.createLessonTask(request,lessonId);
    }

    @PostMapping("/create-one")
    @Operation(summary = "과제 생성",
            description = "과제를 한명에게만 생성합니다.")
    public TaskResponse createEachTask(@RequestBody CreateEachTaskRequest request) {
        return taskService.createEachTask(request);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "과제 검색",
            description = "과제를 id로 검색합니다.")
    public TaskResponse findTaskById(@PathVariable Long id) { //우리가 잠시 사용할 기능
        return taskService.findTaskById(id);
    }

    @GetMapping("/title/{title}")
    @Operation(summary = "과제 검색",
            description = "과제를 과제의 제목으로 검색합니다.")
    public TaskResponse findTaskByTitle(@PathVariable String title) { //유저가 사용할 기능
        return taskService.findTaskByTitle(title);
    }

    @PatchMapping()
    @Operation(summary = "과제 수정",
            description = "과제를 수정합니다.")
    public TaskResponse updateTask(@RequestBody UpdateTaskRequest request) { return taskService.updateTask(request); }

    @DeleteMapping("/{id}")
    @Operation(summary = "과제 삭제",
            description = "과제를 하나 삭제합니다.")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }


    @GetMapping("/all")
    @Operation(summary = "과제 검색",
            description = "모든 과제를 검색합니다.")
    public List<TaskResponse> findAllTask() {
        return taskService.findAll();
    }

}