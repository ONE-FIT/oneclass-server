package oneclass.oneclass.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.service.AdminAttendanceService;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AdminAttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    /**
     * ✅ QR 이미지 생성 (교사용)
     */
    @GetMapping(value = "/qr/{lessonId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQr(@PathVariable Long lessonId) {
        byte[] qrImage = attendanceService.generateAttendanceQrPng(lessonId, 1); // 1분 유효
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
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
        boolean valid = attendanceService.verifyNonce(nonce, lessonId);
        if (!valid) {
            return ResponseEntity.badRequest().body("Invalid or expired QR code");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Attendance attendance = Attendance.builder()
                .member(member)
                .date(LocalDate.now())
                .attendanceStatus(AttendanceStatus.PRESENT)
                .build();

        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance recorded successfully");
    }
}