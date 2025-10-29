package oneclass.oneclass.domain.announce.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CreateAnnounceRequest(
        String title,
        String content,
        Boolean important,
        String reservation
) {
}