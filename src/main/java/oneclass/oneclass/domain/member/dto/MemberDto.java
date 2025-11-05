package oneclass.oneclass.domain.member.dto;

import oneclass.oneclass.domain.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Role role;
}