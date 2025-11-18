package oneclass.oneclass.domain.sendon.event;

public record AnnounceForMemberSavedEvent(
        String content,
        String title,
        Long memberId
) {
}
