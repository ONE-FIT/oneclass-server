package oneclass.oneclass.domain.message.sms.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnnounceSavedEvent {
    private final String content;
    private final String title;

}
