package oneclass.oneclass.domain.task.error;

import oneclass.oneclass.global.exception.CustomError;

public enum TaskError implements CustomError {

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