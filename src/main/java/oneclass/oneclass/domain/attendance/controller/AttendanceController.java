package oneclass.oneclass.domain.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.response.AttendanceResponse;
import oneclass.oneclass.domain.attendance.service.AttendanceService;
import oneclass.oneclass.global.auth.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    @Operation(summary = "Qr 생성",
            description = "Qr을 생성합니다.")
    public ResponseEntity<byte[]> generateQr(@PathVariable Long lessonId) {
        byte[] qrImage = attendanceService.generateAttendanceQrPng(lessonId, qrValidityMinutes);
        return ResponseEntity.ok(qrImage);
    }
    /** ✅ 학생이 QR을 스캔하면 nonce + lessonId만 전송 → 로그인 정보로 출석 처리 */
    @PostMapping("/check")
    @Operation(summary = "학생 출석",
            description = "Qr을 스캔하면 출석처리합니다.")
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
    @Operation(summary = "Qr이미지 ",
            description = "캐시에 저장된 최신 QR 코드 이미지를 반환합니다.")
    public ResponseEntity<byte[]> getQr(@PathVariable Long lessonId) {
        return ResponseEntity.ok(attendanceService.getCachedQr(lessonId));
    }

    /** Admin-only endpoints */@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/today/present/{lessonId}")
    @Operation(summary = "오늘 출석한 학생 목록 조회",
            description = "특정 수업에 대해 오늘 출석한 학생 목록을 반환합니다.")
    public ResponseEntity<List<AttendanceResponse>> getTodayPresentMembers(
            @PathVariable("lessonId") Long lessonId
    ) {
        List<AttendanceResponse> presentList =
                attendanceService.getTodayPresentMembers(lessonId);
        return ResponseEntity.ok(presentList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/today/absent/{lessonId}")
    @Operation(summary = "오늘 결석한 학생 목록 조회",
            description = "특정 수업에 대해 오늘 결석한 학생 목록을 반환합니다.")
    public ResponseEntity<List<AttendanceResponse>> getTodayAbsentMembers(
            @PathVariable("lessonId") Long lessonId
    ) {
        List<AttendanceResponse> absentList =
                attendanceService.getTodayAbsentMembers(lessonId, LocalDate.now());
        return ResponseEntity.ok(absentList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{lessonId}/date/{date}")
    @Operation(summary = "특정 날짜 출석 목록 조회",
            description = "특정 수업과 날짜에 대한 출석 목록을 반환합니다.")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("date") String date
    ) {
        LocalDate localDate = LocalDate.parse(date);
        List<AttendanceResponse> attendanceList =
                attendanceService.getAttendanceByDate(lessonId, localDate);
        return ResponseEntity.ok(attendanceList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{lessonId}/all")
    @Operation(summary = "전체 출석 목록 조회",
            description = "특정 수업에 대한 전체 출석 기록을 반환합니다.")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance(
            @PathVariable("lessonId") Long lessonId
    ) {
        List<AttendanceResponse> attendanceList = attendanceService.getAllAttendance(lessonId);
        return ResponseEntity.ok(attendanceList);
    }
}