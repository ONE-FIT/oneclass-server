package oneclass.oneclass.domain.member.dto.response;

import oneclass.oneclass.domain.member.entity.Role;

public record MemberDto(
        Long id,
        String username,
        String name,
        String phone,
        Role role
) { }