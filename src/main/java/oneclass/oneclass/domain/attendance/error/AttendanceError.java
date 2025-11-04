// AttendanceError.java
package oneclass.oneclass.domain.attendance.error;

import oneclass.oneclass.global.exception.CustomError;

public enum AttendanceError implements CustomError {
    NOT_FOUND(404, "출석 기록을 찾을 수 없습니다."),
    INVALID_DATE_FORMAT(400, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요."),
    INVALID_STATUS(400, "잘못된 출석 상태입니다."),
    ALREADY_USED(400,"이미 사용되었습니다."),
    EXPIRED(404,"이미 삭제되었습니다."),
    ALREADY_ATTENDED(400,"이미 출석되었습니다."),
    INVALID_LESSON(404,"강의를 찾을 수 없습니다.");
    private final int status;
    private final String message;

    AttendanceError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
