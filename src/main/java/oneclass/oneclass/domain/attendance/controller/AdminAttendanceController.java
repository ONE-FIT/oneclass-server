package oneclass.oneclass.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.service.AdminAttendanceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AdminAttendanceService attendanceService;

    @GetMapping
    public List<AttendanceResponse> getAttendanceByStatus(@RequestParam AttendanceStatus status) {
        return attendanceService.getTodayMembersByStatus(status);
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

//    @GetMapping("/all")
//    public List<AttendanceResponse> getAllAttendanceRecords() {
//        return attendanceService.getAllAttendanceRecords();
//    }
}
