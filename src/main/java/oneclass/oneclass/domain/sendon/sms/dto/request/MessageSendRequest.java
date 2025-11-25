package oneclass.oneclass.domain.sendon.sms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MessageSendRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotBlank(message = "메시지는 필수입니다.")
    private String message;
    
    @NotEmpty(message = "전화번호 리스트는 비어있을 수 없습니다.")
    private List<String> phoneNumbers;
}
