package oneclass.oneclass.domain.attendance.dto.response;

import lombok.Builder;

@Builder
public record QrCodeResponse(
        String code
) {
}
