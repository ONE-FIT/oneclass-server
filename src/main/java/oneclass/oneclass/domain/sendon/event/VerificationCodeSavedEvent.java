package oneclass.oneclass.domain.sendon.event;

import java.time.LocalDateTime;

public record VerificationCodeSavedEvent(
        String TempCode, String phone
) {
}
