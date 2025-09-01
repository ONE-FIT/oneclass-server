package oneclass.oneclass.domain.student.attendance.dto.response;

import lombok.Builder;

@Builder
public record QrCodeResponse(
        String code
) {
}
