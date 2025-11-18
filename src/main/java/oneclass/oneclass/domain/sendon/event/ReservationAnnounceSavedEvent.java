package oneclass.oneclass.domain.sendon.event;

public record ReservationAnnounceSavedEvent(
        String content, String title, String reservation
) {
}
