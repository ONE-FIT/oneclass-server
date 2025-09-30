package oneclass.oneclass.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    // 오늘 특정 상태의 출석 정보 조회 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayMembersByStatus(AttendanceStatus status) {
        return attendanceRepository.findByDateAndAttendanceStatus(LocalDate.now(), status)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    // 오늘 출석한 사람들 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayPresentMembers() {
        return getTodayMembersByStatus(AttendanceStatus.PRESENT);
    }

    // 오늘 결석한 사람들 (AttendanceResponse 반환)
    public List<AttendanceResponse> getTodayAbsentMembers() {
        List<Member> allMembers = memberRepository.findAll();
        List<Long> presentMemberIds = attendanceRepository.findByDateAndAttendanceStatus(LocalDate.now(), AttendanceStatus.PRESENT)
                .stream()
                .map(a -> a.getMember().getId())
                .toList();
        return allMembers.stream()
                .filter(m -> !presentMemberIds.contains(m.getId()))
                .map(m -> new AttendanceResponse(m.getUsername(), AttendanceStatus.ABSENT, LocalDate.now()))
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

    // 전체 출석 정보 (AttendanceResponse 반환)
    public List<AttendanceResponse> getAllAttendanceRecords() {
        return attendanceRepository.findAll()
                .stream()
                .map(this::attendanceToResponse)
                .toList();
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
}
