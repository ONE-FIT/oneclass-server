package oneclass.oneclass.domain.lesson.error;

import oneclass.oneclass.global.exception.CustomError;

public enum LessonError implements CustomError {

    NOT_FOUND(404, "강의를 찾을 수 없습니다."),
    INVALID_LESSON_ID_VALUE(400, "강의 ID가 입력되지 않았습니다."),
    
    // Validation 에러
    TITLE_REQUIRED(400, "강의 제목은 필수입니다."),
    TITLE_TOO_LONG(400, "강의 제목은 100자를 초과할 수 없습니다."),
    TEACHER_ID_REQUIRED(400, "선생님 ID는 필수입니다."),
    INVALID_TEACHER_ID(400, "유효하지 않은 선생님 ID입니다.");

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
