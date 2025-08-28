package oneclass.oneclass.domain.attendence.client.dto.response;

import lombok.Builder;

@Builder
public record QrCodeResponse(
        String code
) {
}
