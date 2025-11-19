package oneclass.oneclass.domain.task.error;

import oneclass.oneclass.global.exception.CustomError;

public enum TaskError implements CustomError {

    ASSIGNMENT_NOT_FOUND(404, "해당 학생의 과제를 찾을 수 없습니다."),
    NOT_FOUND(404, "과제를 찾을 수 없습니다."),
    
    // Validation 에러
    TITLE_REQUIRED(400, "과제 제목은 필수입니다."),
    TITLE_TOO_LONG(400, "과제 제목은 100자를 초과할 수 없습니다."),
    DESCRIPTION_TOO_LONG(400, "과제 설명은 1000자를 초과할 수 없습니다."),
    DUE_DATE_REQUIRED(400, "과제 마감일은 필수입니다."),
    INVALID_DUE_DATE(400, "과제 마감일은 현재 날짜 이후여야 합니다."),
    TEACHER_ID_REQUIRED(400, "선생님 ID는 필수입니다."),
    STUDENT_ID_REQUIRED(400, "학생 ID는 필수입니다."),
    INVALID_TEACHER_ID(400, "유효하지 않은 선생님 ID입니다."),
    INVALID_STUDENT_ID(400, "유효하지 않은 학생 ID입니다.");

    private final int status;
    private final String message;

    TaskError(int status, String message) {
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