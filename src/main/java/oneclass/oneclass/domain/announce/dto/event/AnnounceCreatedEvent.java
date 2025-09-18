package oneclass.oneclass.domain.announce.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnnounceCreatedEvent {
    private final String content;
    private final String title;

}
