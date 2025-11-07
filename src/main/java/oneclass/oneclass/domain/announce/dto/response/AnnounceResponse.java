package oneclass.oneclass.domain.announce.dto.response;

import oneclass.oneclass.domain.announce.entity.Announce;

public record AnnounceResponse (
        Long id,
        String title,
        String content
){
    public static AnnounceResponse of (Announce announce) {
        return new AnnounceResponse(
                announce.getId(),
                announce.getTitle(),
                announce.getContent()
        );
    }
}