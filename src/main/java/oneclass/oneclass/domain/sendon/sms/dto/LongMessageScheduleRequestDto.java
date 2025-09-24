package oneclass.oneclass.domain.sendon.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

// DTO 정의
@Setter
@Getter
public class LongMessageScheduleRequestDto {
    // getter/setter
    @NotBlank
    private String message;

    @NotBlank
    private String title;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservation;

}