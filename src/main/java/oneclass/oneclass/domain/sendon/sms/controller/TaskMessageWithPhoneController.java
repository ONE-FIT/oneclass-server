package oneclass.oneclass.domain.sendon.sms.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.sms.dto.request.ShortMessageSendWithPhoneRequest;
import oneclass.oneclass.domain.sendon.sms.dto.response.MessageSendResponse;
import oneclass.oneclass.domain.sendon.sms.shortmessage.SmsSendShortMessageToPhoneNumbers;
import oneclass.oneclass.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskMessageWithPhoneController {

    private final SmsSendShortMessageToPhoneNumbers smsSendShortMessageToPhoneNumbers;

    @PostMapping("task/sms/send")
    public ResponseEntity<ApiResponse<MessageSendResponse>> sendMessage(
            @RequestBody @Validated ShortMessageSendWithPhoneRequest request
    ) {
        smsSendShortMessageToPhoneNumbers.send(
                request.getMessage(),
                request.getTitle(),
                request.getPhoneNumbers()
        );
        
        MessageSendResponse response = new MessageSendResponse(
                request.getTitle(),
                request.getMessage(),
                request.getPhoneNumbers().size(),
                request.getPhoneNumbers()
        );
        
        return ResponseEntity.accepted().body(ApiResponse.success(response));
    }
}
