package oneclass.oneclass.domain.announce.dto.request;

public record UpdateAnnounceRequest(
        Long id,
        String title,
        String content,
        Boolean important
) {

}