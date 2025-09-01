package oneclass.oneclass.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.auth.member.entity.Member;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;

    // 오늘 특정 상태의 사람들 조회 (공통 메서드)
    public List<Member> getTodayMembersByStatus(AttendanceStatus status) {
        return attendanceRepository.findByDateAndAttendanceStatus(LocalDate.now(), status)
                .stream()
                .map(Attendance::getMember)
                .collect(Collectors.toList());
    }

    // 오늘 출석한 사람
    public List<Member> getTodayPresentMembers() {
        return getTodayMembersByStatus(AttendanceStatus.PRESENT);
    }

    // 오늘 결석한 사람
    public List<Member> getTodayAbsentMembers(List<Member> allMembers) {
        List<Member> presentMembers = getTodayPresentMembers();
        return allMembers.stream()
                .filter(m -> presentMembers.stream().noneMatch(p -> p.getId().equals(m.getId())))
                .collect(Collectors.toList());
    }

    // 오늘 지각한 사람
    public List<Member> getTodayLateMembers() {
        return getTodayMembersByStatus(AttendanceStatus.LATE);
    }

    // 오늘 공결 처리된 사람
    public List<Member> getTodayExcusedMembers() {
        return getTodayMembersByStatus(AttendanceStatus.EXCUSED);
    }

    // 전체 출석 정보
    public List<Attendance> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    // 특정 학생 출석 기록
    public List<Attendance> getAttendanceByMember(Member member) {
        return attendanceRepository.findByMember(member);
    }

    // 특정 날짜 출석 기록
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }
}
