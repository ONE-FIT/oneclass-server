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

    private String name; // 학생 이름
    private String lessonTitle; // ✅ 반(레슨) 이름 추가
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus; // PRESENT / ABSENT / LATE / EXCUSED

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public AttendanceResponse(String name, String lessonTitle, AttendanceStatus attendanceStatus, LocalDate date) {
        this.name = name;
        this.lessonTitle = lessonTitle;
        this.attendanceStatus = attendanceStatus;
        this.date = date;
    }

    public static AttendanceResponse fromEntity(Attendance attendance) {
        var member = attendance.getMember();
        var lesson = member.getLesson();
        return new AttendanceResponse(
                member.getName(),
                lesson != null ? lesson.getTitle() : "미배정",
                attendance.getAttendanceStatus(),
                attendance.getDate()
        );
    }
}