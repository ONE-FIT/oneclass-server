// MemberDto.java
package oneclass.oneclass.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import oneclass.oneclass.global.auth.member.entity.Member;

@Getter
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String name;

    // 엔티티 -> DTO 변환
    public static TaskResponse fromEntity(Member member) {
        return new TaskResponse(
                member.getId(),
                member.getUsername()
        );
    }
}
