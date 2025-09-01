package oneclass.oneclass.domain.admin.announce.dto.request;

public record UpdateAnnounceRequest(
        Long id,
        String title,
        String content
) {

}
