package oneclass.oneclass.domain.announce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAnnounceRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Boolean important,
        @NotBlank String reservation,
        @NotNull Long lessonId // ✅ 추가
) {
}