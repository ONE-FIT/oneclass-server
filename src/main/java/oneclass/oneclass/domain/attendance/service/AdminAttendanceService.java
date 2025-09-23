package oneclass.oneclass.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    public List<AttendanceResponse> getTodayMembersByStatus(AttendanceStatus status) {
        return getMembersByDateAndStatus(LocalDate.now(), status);
    }

    public List<AttendanceResponse> getMembersByDateAndStatus(LocalDate date, AttendanceStatus status) {
        if (status == AttendanceStatus.ABSENT) {
            // 결석 처리
            List<Long> presentMemberIds = attendanceRepository.findByDateAndAttendanceStatus(date, AttendanceStatus.PRESENT)
                    .stream()
                    .map(a -> a.getMember().getId())
                    .toList();
            return memberRepository.findAll().stream()
                    .filter(m -> !presentMemberIds.contains(m.getId()))
                    .map(m -> new AttendanceResponse(m.getUsername(), AttendanceStatus.ABSENT, date))
                    .toList();
        } else {
            return attendanceRepository.findByDateAndAttendanceStatus(date, status)
                    .stream()
                    .map(this::attendanceToResponse)
                    .toList();
        }
    }

    public List<AttendanceResponse> getAllAttendanceRecords() {
        return attendanceRepository.findAll().stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    public List<AttendanceResponse> getAttendanceByMember(Long memberId) {
        return attendanceRepository.findByMemberId(memberId)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream()
                .map(this::attendanceToResponse)
                .toList();
    }

    private AttendanceResponse attendanceToResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getMember().getUsername(),
                attendance.getAttendanceStatus(),
                attendance.getDate()
        );
    }
}
