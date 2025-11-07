package oneclass.oneclass.domain.attendance.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class AttendanceResponse {
    private Long id;

    private String name;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus; // PRESENT / ABSENT / LATE / EXCUSED

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public AttendanceResponse(Long id, String username, AttendanceStatus attendanceStatus, LocalDate now) {
        this.id = id;
        this.name = username;
        this.attendanceStatus = attendanceStatus;
        this.date = now;
    }
}