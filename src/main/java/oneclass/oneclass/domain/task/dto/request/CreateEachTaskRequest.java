package oneclass.oneclass.domain.task.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEachTaskRequest(
        @NotBlank(message = "과제 제목은 필수입니다.")
        @Size(max = 100, message = "과제 제목은 100자를 초과할 수 없습니다.")
        String title,
        
        @Size(max = 1000, message = "과제 설명은 1000자를 초과할 수 없습니다.")
        String description,
        
        @NotNull(message = "과제 마감일은 필수입니다.")
        @Future(message = "과제 마감일은 현재 날짜 이후여야 합니다.")
        LocalDate dueDate,
        
        @NotNull(message = "선생님 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 선생님 ID입니다.")
        Long teacherId,
        
        @NotNull(message = "학생 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 학생 ID입니다.")
        Long studentId
) {
}
