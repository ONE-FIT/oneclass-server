package oneclass.oneclass.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.service.AdminAttendanceService;
import oneclass.oneclass.domain.auth.member.entity.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AdminAttendanceService attendanceService;

    // 오늘 출석한 학생 조회
    @GetMapping("/present")
    public List<String> getTodayPresentMembers() {
        return attendanceService.getTodayPresentMembers().stream().map(
                Member::getUsername
        ).toList();
    }

    // 오늘 결석한 학생 조회
    // allMembers는 일반적으로 MemberRepository에서 전체 학생 조회해서 넘기는 구조가 필요
    @GetMapping("/absent")
    public List<String> getTodayAbsentMembers() {
        return attendanceService.getTodayAbsentMembers();
    }

    // 오늘 지각한 학생 조회
    @GetMapping("/late")
    public List<String> getTodayLateMembers() {
        return AdminAttendanceService.memberEntityToString(attendanceService.getTodayLateMembers());
    }

    // 오늘 공결 처리된 학생 조회
    @GetMapping("/excused")
    public List<String> getTodayExcusedMembers() {
        return AdminAttendanceService.memberEntityToString(attendanceService.getTodayExcusedMembers());
    }

    // TODO: 전용 dto 필요

    // 특정 학생 출석 기록 조회
    @GetMapping("/member/{memberId}")
    public List<Attendance> getAttendanceByMember(@PathVariable Long memberId) {
        Member member = new Member(); // 실제로는 MemberRepository에서 찾아야 함
        member.setId(memberId);
        return attendanceService.getAttendanceByMember(member);
    }

    // 특정 날짜 출석 기록 조회
    @GetMapping("/date/{date}")
    public List<Attendance> getAttendanceByDate(@PathVariable String date) {
        return attendanceService.getAttendanceByDate(LocalDate.parse(date));
    }

    // 전체 출석 기록 조회
    @GetMapping("/all")
    public List<Attendance> getAllAttendanceRecords() {
        return attendanceService.getAllAttendanceRecords();
    }
}
