package oneclass.oneclass.announce.dto.request;

public record UpdateAnnounceRequest(
        Long id,
        String title,
        String content
) {

}
