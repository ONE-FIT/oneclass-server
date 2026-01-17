package oneclass.oneclass.domain.lesson.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateLessonRequest(
        @NotBlank(message = "강의 제목은 필수입니다.")
        @Size(max = 100, message = "강의 제목은 100자를 초과할 수 없습니다.")
        String title,
        
        @NotNull(message = "선생님 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 선생님 ID입니다.")
        Long teacherId
) {
}
