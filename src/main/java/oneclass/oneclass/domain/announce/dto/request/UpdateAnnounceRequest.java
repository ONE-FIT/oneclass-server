package oneclass.oneclass.domain.announce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAnnounceRequest(
        @NotNull Long id,
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Boolean important,
        @NotNull Long memberId
) {

}