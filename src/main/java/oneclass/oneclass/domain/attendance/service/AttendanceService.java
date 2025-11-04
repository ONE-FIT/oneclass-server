package oneclass.oneclass.domain.attendance.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.attendance.dto.response.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceNonce;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.error.AttendanceError;
import oneclass.oneclass.domain.attendance.repository.AttendanceNonceRepository;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j

public class AttendanceService {

    // ✅ QR 캐시: lessonId -> QR 이미지(byte[]) 저장
    private final Map<Long, byte[]> qrCache = new ConcurrentHashMap<>();
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final AttendanceNonceRepository nonceRepository;

    // ✅ 오늘 특정 상태의 출석 정보 조회
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

    // ✅ 오늘 출석한 사람들
    public List<AttendanceResponse> getTodayPresentMembers() {
        return getTodayMembersByStatus(AttendanceStatus.PRESENT);
    }

    // ✅ 오늘 결석한 사람들
    public List<AttendanceResponse> getTodayAbsentMembers(LocalDate date) {
        List<Member> absentMembers = memberRepository.findAbsentMembers(date);

        return absentMembers.stream()
                .map(m -> new AttendanceResponse(m.getName(), AttendanceStatus.ABSENT, date))
                .toList();
    }

    // ✅ 오늘 지각한 사람들
    public List<AttendanceResponse> getTodayLateMembers() {
        return getTodayMembersByStatus(AttendanceStatus.LATE);
    }

    // ✅ 오늘 공결한 사람들
    public List<AttendanceResponse> getTodayExcusedMembers() {
        return getTodayMembersByStatus(AttendanceStatus.EXCUSED);
    }

    // ✅ 특정 학생 출석 기록
    public List<AttendanceResponse> getAttendanceByMember(Long memberId) {
        return attendanceRepository.findByMemberId(memberId)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    // ✅ 특정 날짜 출석 기록
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    // ✅ 엔티티 → DTO 변환 메서드
    private AttendanceResponse attendanceToResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getMember().getName(),
                attendance.getAttendanceStatus(),
                attendance.getDate()
        );
    }

    // ✅ --- QR 코드 + nonce 저장/검증 기능 ---

    /**
     * QR 생성 시 nonce를 DB에 저장하고 QR 이미지를 반환합니다.
     * @param lessonId QR을 생성할 수업 ID
     * @param validMinutes QR 유효 시간 (분 단위)
     */
    public byte[] generateAttendanceQrPng(Long lessonId, int validMinutes) {
        LocalDateTime now = LocalDateTime.now();

        Optional<AttendanceNonce> opt = nonceRepository.findTopByLessonIdAndUsedFalseOrderByCreatedAtDesc(lessonId);
        AttendanceNonce current;

        if (opt.isPresent()) {
            current = opt.get();
            current.setNonce(UUID.randomUUID().toString());
            current.setCreatedAt(now);
            current.setExpireAt(now.plusMinutes(validMinutes));
        } else {
            // 처음 생성 시
            current = AttendanceNonce.builder()
                    .lessonId(lessonId)
                    .nonce(UUID.randomUUID().toString())
                    .createdAt(now)
                    .expireAt(now.plusMinutes(validMinutes))
                    .used(false)
                    .build();
        }

        nonceRepository.save(current);

        // ✅ QR 이미지 생성 및 캐시 저장
        byte[] qrCodeImage = createQrImage(
                String.format("{\"type\":\"attendance\",\"lessonId\":%d,\"nonce\":\"%s\"}",
                        lessonId, current.getNonce()),
                350, 350
        );
        qrCache.put(lessonId, qrCodeImage);

        log.info("Generated & cached QR for lessonId {} at {}", lessonId, now);
        return qrCodeImage;
    }

    /**
     * 학생이 QR을 스캔한 후 서버로 보낸 nonce를 검증합니다.
     * @return 검증 성공 여부
     */
    @Transactional
    public boolean verifyNonce(String nonce, Long lessonId) {
        AttendanceNonce attendanceNonce = nonceRepository.findByNonce(nonce)
                .orElseThrow(() -> new CustomException(AttendanceError.NOT_FOUND)); // or new InvalidNonceException()
        if (attendanceNonce.isUsed()) {
            throw new CustomException(AttendanceError.ALREADY_USED); // 예시: 에러 타입 분리
        }
        if (attendanceNonce.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(AttendanceError.EXPIRED); // 예시: 에러 타입 분리
        }
        if (!attendanceNonce.getLessonId().equals(lessonId)) {
            throw new CustomException(AttendanceError.INVALID_LESSON); // 예시: 에러 타입 분리
        }
        // ✅ 검증 성공 → 사용 처리
        attendanceNonce.setUsed(true);
        nonceRepository.save(attendanceNonce);

        return true;
    }

    // ✅ QR 이미지 생성 헬퍼
    private byte[] createQrImage(String text, int width, int height) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            com.google.zxing.common.BitMatrix matrix = new com.google.zxing.MultiFormatWriter()
                    .encode(text, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }
    }

    @Transactional
    public String recordAttendance(String nonce, Long lessonId, Long memberId) {
        // ✅ 1. nonce 검증
        verifyNonce(nonce, lessonId);

        // ✅ 2. member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        // ✅ 3. 중복 출석 방지
        boolean alreadyExists = attendanceRepository
                .findByMemberIdAndDate(memberId, LocalDate.now())
                .isPresent();
        if (alreadyExists) {
            throw new CustomException(AttendanceError.ALREADY_ATTENDED); // 예시: CustomException 사용
        }

        // ✅ 4. 출석 저장
        Attendance attendance = Attendance.builder()
                .member(member)
                .date(LocalDate.now())
                .attendanceStatus(AttendanceStatus.PRESENT)
                .build();

        attendanceRepository.save(attendance);
        return "Attendance recorded successfully";
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void regenerateQrCodes() {
        // Rotate the current active (unused & unexpired) nonce for each lesson instead of inserting a new row
        LocalDateTime now = LocalDateTime.now();
        List<Long> activeLessonIds = nonceRepository.findActiveLessonIds(now);
        int validMinutes = 1; // QR code validity in minutes (update every minute)

        for (Long lessonId : activeLessonIds) {
            // find the latest active nonce for this lesson (if any)
            Optional<AttendanceNonce> opt = nonceRepository.findTopByLessonIdAndUsedFalseOrderByCreatedAtDesc(lessonId);
            if (opt.isEmpty()) {
                // no existing active nonce to rotate — skip
                continue;
            }

            AttendanceNonce current = opt.get();
            // rotate the nonce (replace value and extend expiry)
            current.setNonce(UUID.randomUUID().toString());
            current.setCreatedAt(now);
            current.setExpireAt(now.plusMinutes(validMinutes));

            nonceRepository.save(current);
            log.info("Rotated QR nonce for lessonId {} at {}", lessonId, now);
        }
    }
    // ✅ 최신 QR을 반환하는 메서드
    public byte[] getCachedQr(Long lessonId) {
        return qrCache.getOrDefault(lessonId, new byte[0]);
    }
}