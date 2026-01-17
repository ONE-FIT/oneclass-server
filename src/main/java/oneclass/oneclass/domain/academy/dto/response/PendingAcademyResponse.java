package oneclass.oneclass.domain.academy.dto.response;

import oneclass.oneclass.domain.academy.entity.Academy;

public record PendingAcademyResponse(
        String academyCode,
        String academyName,
        String email,
        String phone,
        Academy.Status status
) {
}