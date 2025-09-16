// MemberDto.java
package oneclass.oneclass.domain.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import oneclass.oneclass.global.auth.member.entity.Member;


@Getter
@AllArgsConstructor
public class MemberTaskResponse {
    private Long id;
    private String name;

    // 엔티티 -> DTO 변환
    public static MemberTaskResponse fromEntity(Member member) {
        return new MemberTaskResponse(
                member.getId(),
                member.getUsername()
        );
    }
}
