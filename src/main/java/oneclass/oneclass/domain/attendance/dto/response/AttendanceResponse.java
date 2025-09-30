package oneclass.oneclass.domain.attendance.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceResponse {
    public String name;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus; // PRESENT / ABSENT / LATE / EXCUSED

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public AttendanceResponse(String username, AttendanceStatus attendanceStatus, LocalDate now) {
        this.name = username;
        this.attendanceStatus = attendanceStatus;
        this.date = now;
    }
}