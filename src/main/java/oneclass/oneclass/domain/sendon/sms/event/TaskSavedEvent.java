package oneclass.oneclass.domain.sendon.sms.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskSavedEvent {
    private final String description;
    private final String title;

}