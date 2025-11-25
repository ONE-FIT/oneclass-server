package oneclass.oneclass.domain.sendon.sms.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.sms.dto.request.LongMessageScheduleRequest;
import oneclass.oneclass.domain.sendon.sms.dto.response.LongMessageScheduleResponse;
import oneclass.oneclass.domain.sendon.sms.longmessage.SmsSendLongMessageSchedule;
import oneclass.oneclass.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LongMessageScheduleController {

    private final SmsSendLongMessageSchedule smsSendLongMessageScheduleScenario;

    // @param reservation 예약 시간 (yyyy-MM-dd HH:mm:ss)
    @PostMapping("/lms/schedule")
    public ResponseEntity<ApiResponse<LongMessageScheduleResponse>> sendLongMessage(
            @RequestBody @Validated LongMessageScheduleRequest dto
    ) {
        smsSendLongMessageScheduleScenario.send(dto.getMessage(), dto.getTitle(), dto.getReservation().toString());
        return ResponseEntity.ok(ApiResponse.success(new LongMessageScheduleResponse(dto.getMessage(), dto.getTitle(), dto.getReservation())));
    }
}