package oneclass.oneclass.domain.sendon.sms.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.sms.shortmessage.SmsResetPasswordCode;
import oneclass.oneclass.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortMessageTestController {

    private final SmsResetPasswordCode smsResetPasswordCode;

    @PostMapping("sms-test")
    public ResponseEntity<ApiResponse<String>> shortMessageTestController() {
        String response = smsResetPasswordCode.execute("hello", "01092973629");
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
