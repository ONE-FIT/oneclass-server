package oneclass.oneclass.domain.lesson.error;

import oneclass.oneclass.global.exception.CustomError;

public enum LessonError implements CustomError {

    NOT_FOUND(404, "강의를 찾을 수 없습니다."),
    INVALID_MEMBER_ID_VALUE(400, "강의 ID가 입력되지 않았습니다.");

    private final int status;
    private final String message;

    LessonError(int status, String message) {
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
