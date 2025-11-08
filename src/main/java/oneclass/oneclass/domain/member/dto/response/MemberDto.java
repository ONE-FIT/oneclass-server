package oneclass.oneclass.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import oneclass.oneclass.domain.member.entity.Role;

@Getter
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Role role;
}