package oneclass.oneclass.domain.attendance.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.response.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    // 오늘 특정 상태의 출석 정보 조회 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayMembersByStatus(AttendanceStatus status) {
        final LocalDate today = LocalDate.now();
        if (status == AttendanceStatus.ABSENT) {
            return getTodayAbsentMembers(today); // 결석은 별도 로직
        }
        return attendanceRepository.findByDateAndAttendanceStatus(today, status)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }


    // 오늘 출석한 사람들 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayPresentMembers() {
        return getTodayMembersByStatus(AttendanceStatus.PRESENT);
    }

    // 오늘 결석한 사람들 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayAbsentMembers(LocalDate date) {
        List<Member> absentMembers = memberRepository.findAbsentMembers(date);

        return absentMembers.stream()
                .map(m -> new AttendanceResponse(m.getUsername(), AttendanceStatus.ABSENT, date))
                .toList();
    }

    // 오늘 지각한 사람들 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayLateMembers() {
        return getTodayMembersByStatus(AttendanceStatus.LATE);
    }

    // 오늘 공결 처리된 사람들 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayExcusedMembers() {
        return getTodayMembersByStatus(AttendanceStatus.EXCUSED);
    }

    // 특정 학생 출석 기록 (AttendanceResponse 반환)
    public List<AttendanceResponse> getAttendanceByMember(Long memberId) {
        return attendanceRepository.findByMemberId(memberId)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    // 특정 날짜 출석 기록 (AttendanceResponse 반환)
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    // 엔티티 -> DTO 변환 메서드
    private AttendanceResponse attendanceToResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getMember().getUsername(),
                attendance.getAttendanceStatus(),
                attendance.getDate()
        );
    }


    // --- QR 코드 생성 관련 메서드 추가 ---

    /**
     * 수업용 출석 QR 페이로드를 생성하고 PNG 바이트 배열을 반환합니다.
     * 페이로드 예시: {"type":"attendance","lessonId":123,"date":"2025-10-20","nonce":"uuid"}
     */
    public byte[] generateAttendanceQrPng(Long lessonId, LocalDate date) {
        String payload = buildAttendancePayload(lessonId, date);
        return createQrImage(payload, 350, 350);
    }

    /**
     * Base64 인코딩된 PNG 문자열 반환 (웹에서 <img src="data:image/png;base64,...">로 바로 사용 가능)
     */
    public String generateAttendanceQrBase64(Long lessonId, LocalDate date) {
        byte[] png = generateAttendanceQrPng(lessonId, date);
        return Base64.getEncoder().encodeToString(png);
    }

    private String buildAttendancePayload(Long lessonId, LocalDate date) {
        String nonce = UUID.randomUUID().toString();
        // TODO: nonce를 DB에 저장해서 재사용/재발급 방지, 만료 시간 체크 로직 추가 권장
        org.json.simple.JSONObject payload = new org.json.simple.JSONObject();
        payload.put("type", "attendance");
        payload.put("lessonId", lessonId);
        payload.put("date", date.toString());
        payload.put("nonce", nonce);
        return payload.toJSONString();
    }

    private byte[] createQrImage(String text, int width, int height) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (com.google.zxing.WriterException | java.io.IOException e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }
    }

}
