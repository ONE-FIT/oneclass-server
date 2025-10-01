package oneclass.oneclass.domain.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.response.MemberTaskResponse;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.service.AdminTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    @GetMapping("/{taskId}/members")
    @Operation(summary = "과제별 회원 조회",
            description = "특정 과제 ID에 대해 특정 상태(TaskStatus)에 해당하는 회원 리스트를 반환")
    public List<MemberTaskResponse> getMembersByTaskStatus(
            @Parameter(description = "조회할 과제 ID") @PathVariable Long taskId,
            @Parameter(description = "조회할 과제 상태") @RequestParam TaskStatus status) {
        return adminTaskService.findMemberByTaskStatus(taskId, status);
    }

}
