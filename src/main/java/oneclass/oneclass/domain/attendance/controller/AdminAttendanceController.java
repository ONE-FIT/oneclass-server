package oneclass.oneclass.domain.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.response.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.service.AdminAttendanceService;
import oneclass.oneclass.global.exception.CommonError;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AdminAttendanceService attendanceService;

    @GetMapping
    @Operation(summary = "오늘 출석 상태별 회원 조회",
            description = "오늘 기준으로 특정 상태(PRESENT, ABSENT, LATE 등)에 해당하는 회원 리스트를 반환")
    public List<AttendanceResponse> getAttendanceByStatus(
            @Parameter(description = "조회할 출석 상태") @RequestParam AttendanceStatus status) {

        return attendanceService.getTodayMembersByStatus(status);
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원별 출석 조회",
            description = "특정 회원의 모든 출석 기록을 조회")
    public List<AttendanceResponse> getAttendanceByMember(
            @Parameter(description = "조회할 회원 ID") @PathVariable Long memberId) {
        return attendanceService.getAttendanceByMember(memberId);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "날짜별 출석 조회",
            description = "특정 날짜(YYYY-MM-DD)의 출석 정보를 조회")
    public List<AttendanceResponse> getAttendanceByDate(
            @Parameter(description = "조회할 날짜, 형식: YYYY-MM-DD") @PathVariable String date) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new CustomException(CommonError.INVALID_INPUT_VALUE, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요.");
        }
        return attendanceService.getAttendanceByDate(localDate);
    }
}
