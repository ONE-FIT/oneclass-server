package oneclass.oneclass.domain.task.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.task.dto.TaskResponse;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.service.AdminTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/task")
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    @GetMapping("/{taskId}/members")
    public List<TaskResponse> getMembersByTaskStatus(
            @PathVariable Long taskId,
            @RequestParam TaskStatus status) {
        return adminTaskService.findMemberByTaskStatus(taskId, status);
    }

}
