package oneclass.oneclass.domain.task.error;

import oneclass.oneclass.global.exception.CustomError;

public enum TaskError implements CustomError {

    ASSIGNMENT_NOT_FOUND(404, "해당 학생의 과제를 찾을 수 없습니다."),
    UNAUTHORIZED(403, "해당 과제의 상태를 변경할 권한이 없습니다."),
    NOT_FOUND(404, "과제를 찾을 수 없습니다.");


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