package oneclass.oneclass.domain.sendon.event;

import java.util.List;

public record TaskAssignmentSavedEvent(
        String description, String title, List<Long> memberId
) {
}
