package oneclass.oneclass.domain.sendon.sms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class MessageSendResponse {
    
    private String title;
    private String message;
    private int recipientCount;
    private List<String> phoneNumbers;
}
