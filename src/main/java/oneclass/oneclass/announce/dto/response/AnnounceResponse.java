package oneclass.oneclass.announce.dto.response;

import oneclass.oneclass.announce.entity.Announce;

public record AnnounceResponse (
        Long id,
        String title,
        String content
){
    public static AnnounceResponse of  (Announce announce) {
        return new AnnounceResponse(
                announce.getId(),
                announce.getTitle(),
                announce.getContent()
        );
    }
}
