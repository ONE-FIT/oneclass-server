package oneclass.oneclass.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.service.AdminAttendanceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceQrController {
    private final AdminAttendanceService attendanceService;

    @GetMapping(value = "/qr/{lessonId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQr(@PathVariable Long lessonId) {
        byte[] png = attendanceService.generateAttendanceQrPng(lessonId, LocalDate.now());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    // 또는 Base64 문자열 반환
    @GetMapping("/qr/{lessonId}/base64")
    public String getQrBase64(@PathVariable Long lessonId) {
        return attendanceService.generateAttendanceQrBase64(lessonId, LocalDate.now());
    }
}