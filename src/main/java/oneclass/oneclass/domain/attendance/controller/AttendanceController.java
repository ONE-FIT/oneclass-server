package oneclass.oneclass.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ✅ 설정값 주입
    @Value("${app.qr-code.validity-minutes}")
    private int qrValidityMinutes;

    /** QR 코드 생성 API */
    @GetMapping(value = "/qr/{lessonId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQr(@PathVariable Long lessonId) {
        byte[] qrImage = attendanceService.generateAttendanceQrPng(lessonId, qrValidityMinutes);
        return ResponseEntity.ok(qrImage);
    }
    /**
     * ✅ 학생이 QR을 스캔하면 nonce + lessonId + memberId를 서버로 전송 → 출석 처리
     */
    @PostMapping("/check")
    public ResponseEntity<String> checkAttendance(
            @RequestParam String nonce,
            @RequestParam Long lessonId,
            @RequestParam Long memberId
    ) {
        String result = attendanceService.recordAttendance(nonce, lessonId, memberId);
        return ResponseEntity.ok(result);
    }
}