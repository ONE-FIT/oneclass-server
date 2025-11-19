package oneclass.oneclass.domain.announce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAnnounceRequest(
        @NotBlank(message = "공지 제목은 필수입니다.")
        @Size(max = 200, message = "공지 제목은 200자를 초과할 수 없습니다.")
        String title,
        
        @NotBlank(message = "공지 내용은 필수입니다.")
        @Size(max = 5000, message = "공지 내용은 5000자를 초과할 수 없습니다.")
        String content,
        
        Boolean important,
        String reservation,
        
        @NotNull(message = "강의 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 강의 ID입니다.")
        Long lessonId
) {
}
