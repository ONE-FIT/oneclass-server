package oneclass.oneclass.domain.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.request.CreateEachTaskRequest;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.service.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

        private final TaskService taskService;

        /** 🔹 일반 교사 및 관리자 공용: 레슨 전체 대상 과제 생성 */
        @PostMapping("/create/{lessonId}")
        @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
        @Operation(summary = "과제 생성 (레슨 전체 대상)", description = "레슨에 속한 모든 학생에게 과제를 생성합니다.")
        public TaskResponse createLessonTask(@RequestBody @Valid CreateTaskRequest request, @PathVariable Long lessonId) {
            return taskService.createLessonTask(request, lessonId);
        }

        /** 🔹 개별 과제 생성 */
        @PostMapping("/create-one")
        @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
        @Operation(summary = "개별 과제 생성", description = "특정 학생에게만 과제를 생성합니다.")
        public TaskResponse createEachTask(@RequestBody @Valid CreateEachTaskRequest request) {
            return taskService.createEachTask(request);
        }

        /** 🔹 과제 단건 조회 */
        @GetMapping("/id/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
        @Operation(summary = "과제 ID로 검색", description = "과제를 ID로 검색합니다.")
        public TaskResponse findTaskById(@PathVariable Long id) {
            return taskService.findTaskById(id);
        }

        /** 🔹 제목으로 과제 검색 */
        @GetMapping("/title/{title}")
        @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
        @Operation(summary = "제목으로 과제 검색", description = "과제를 제목으로 검색합니다.")
        public List<TaskResponse> findTaskByTitle(@PathVariable String title) {
            return taskService.findTaskByTitle(title);
        }

        /** 🔹 과제 수정 */
        @PatchMapping
        @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
        @Operation(summary = "과제 수정", description = "과제 정보를 수정합니다.")
        public TaskResponse updateTask(@RequestBody @Valid UpdateTaskRequest request) {
            return taskService.updateTask(request);
        }

        /** 🔹 선생님용: 학생의 과제 상태 수정 */
        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('TEACHER')")
        @Operation(summary = "과제 상태 변경 (선생님용)", description = "선생님이 특정 학생의 과제 상태를 변경합니다.")
        public TaskResponse updateTaskStatus(
                @PathVariable("id") Long taskId,
                @RequestParam Long studentId,
                @RequestParam TaskStatus status
        ) {
            return taskService.updateTaskStatus(taskId, studentId, status);
        }

        /** 🔹 과제 삭제 */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "과제 삭제", description = "관리자가 과제를 삭제합니다.")
        public void deleteTask(@PathVariable Long id) {
            taskService.deleteTask(id);
        }

        /** 🔹 전체 과제 조회 (관리자 전용) */
        @GetMapping("/all")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "전체 과제 조회", description = "관리자가 모든 과제를 조회합니다.")
        public List<TaskResponse> findAllTask() {
            return taskService.findAll();
        }

    }