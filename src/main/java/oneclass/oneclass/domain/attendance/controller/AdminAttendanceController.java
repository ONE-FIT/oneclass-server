package oneclass.oneclass.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.AttendanceResponse;
import oneclass.oneclass.domain.attendance.service.AdminAttendanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AdminAttendanceService attendanceService;

    @GetMapping("/present")
    public List<AttendanceResponse> getTodayPresentMembers() {
        return attendanceService.getTodayPresentMembers();
    }

    @GetMapping("/absent")
    public List<AttendanceResponse> getTodayAbsentMembers() {
        return attendanceService.getTodayAbsentMembers();
    }

    @GetMapping("/late")
    public List<AttendanceResponse> getTodayLateMembers() {
        return attendanceService.getTodayLateMembers();
    }

    @GetMapping("/excused")
    public List<AttendanceResponse> getTodayExcusedMembers() {
        return attendanceService.getTodayExcusedMembers();
    }

    @GetMapping("/member/{memberId}")
    public List<AttendanceResponse> getAttendanceByMember(@PathVariable Long memberId) {
        return attendanceService.getAttendanceByMember(memberId);
    }

    @GetMapping("/date/{date}")
    public List<AttendanceResponse> getAttendanceByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        return attendanceService.getAttendanceByDate(localDate);
    }

    @GetMapping("/all")
    public List<AttendanceResponse> getAllAttendanceRecords() {
        return attendanceService.getAllAttendanceRecords();
    }
}
