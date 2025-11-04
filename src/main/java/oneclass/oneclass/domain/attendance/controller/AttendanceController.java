package oneclass.oneclass.domain.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.service.AttendanceService;
import oneclass.oneclass.global.auth.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Operation(summary = "Qr 생성", description = "Qr을 생성합니다.")
    @GetMapping(value = "/qr/{lessonId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQr(@PathVariable Long lessonId) {
        byte[] qrImage = attendanceService.generateAttendanceQrPng(lessonId, qrValidityMinutes);
        return ResponseEntity.ok(qrImage);
    }
    /** ✅ 학생이 QR을 스캔하면 nonce + lessonId만 전송 → 로그인 정보로 출석 처리 */
    @PostMapping("/check")
    public ResponseEntity<String> checkAttendance(
            @RequestParam String nonce,
            @RequestParam Long lessonId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId(); // 🔒 로그인한 사용자 ID를 가져옴
        String result = attendanceService.recordAttendance(nonce, lessonId, memberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/qr/{lessonId}/cached", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQr(@PathVariable Long lessonId) {
        return ResponseEntity.ok(attendanceService.getCachedQr(lessonId));
    }
}