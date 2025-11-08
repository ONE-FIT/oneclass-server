package oneclass.oneclass.domain.sendon.event;

public record VerificationCodeSavedEvent(
        String tempCode, String phone
) {
}
