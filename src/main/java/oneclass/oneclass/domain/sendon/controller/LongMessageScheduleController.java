package oneclass.oneclass.domain.sendon.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.sms.longmessage.SmsSendLongMessageSchedule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LongMessageScheduleController {

    private final SmsSendLongMessageSchedule smsSendLongMessageScheduleScenario;

    // @param reservation 예약 시간 (yyyy-MM-dd HH:mm:ss)
    @PostMapping("/admin/lms/schedule")
    public ResponseEntity<?> sendLongMessage(
            @RequestParam String message,
            @RequestParam String title,
            @RequestParam String reservation
    ) {
        smsSendLongMessageScheduleScenario.send(message, title, reservation);
        return ResponseEntity.ok("예약 LMS 발송 요청 완료 (" + reservation + ")");
    }
}
