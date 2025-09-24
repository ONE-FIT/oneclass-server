package oneclass.oneclass.domain.sendon.sms.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.sms.dto.LongMessageScheduleRequestDto;
import oneclass.oneclass.domain.sendon.sms.longmessage.SmsSendLongMessageSchedule;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
@RequiredArgsConstructor
public class LongMessageScheduleController {

    private final SmsSendLongMessageSchedule smsSendLongMessageScheduleScenario;

    // @param reservation 예약 시간 (yyyy-MM-dd HH:mm:ss)
    @PostMapping("/lms/schedule")
    public ResponseEntity<?> sendLongMessage(
            @RequestBody @Validated LongMessageScheduleRequestDto dto
    ) {
        smsSendLongMessageScheduleScenario.send(dto.getMessage(), dto.getTitle(), dto.getReservation().toString());
        return ResponseEntity.ok("예약 LMS 발송 요청 완료 (" + dto.getReservation() + ")");
    }
}