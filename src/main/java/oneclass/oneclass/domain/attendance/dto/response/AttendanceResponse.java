package oneclass.oneclass.domain.attendance.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class AttendanceResponse {
  
    private final Long studentId;
    private final String studentName;
    private final String username;

    private final LocalDate date;

    @Enumerated(EnumType.STRING)
    private final AttendanceStatus attendanceStatus; // PRESENT / ABSENT / LATE / EXCUSED

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public AttendanceResponse(Long studentId, String studentName, String username, AttendanceStatus attendanceStatus, LocalDate date) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.username = username;
        this.attendanceStatus = attendanceStatus;
        this.date = date;
    }

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getMember().getName(),
                attendance.getMember().getUsername(),
                attendance.getAttendanceStatus(),
                attendance.getDate()
        );
    }
}