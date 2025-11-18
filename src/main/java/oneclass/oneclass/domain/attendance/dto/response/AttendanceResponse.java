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

    public AttendanceResponse(Long studentId, String studentName, AttendanceStatus attendanceStatus, LocalDate date) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.username = studentName;
        this.attendanceStatus = attendanceStatus;
        this.date = date;
    }

    // 기존 생성자 유지 (하위 호환성)
    @Deprecated
    public AttendanceResponse(String username, AttendanceStatus attendanceStatus, LocalDate date) {
        this.studentId = null;
        this.studentName = username;
        this.username = username;
        this.attendanceStatus = attendanceStatus;
        this.date = date;
    }

    public static AttendanceResponse fromEntity(Attendance attendance) {
        var member = attendance.getMember();
        return new AttendanceResponse(
                member.getId(),  // Long 타입
                member.getName(),  // String
                attendance.getAttendanceStatus(),
                attendance.getDate()
        );
    }
}