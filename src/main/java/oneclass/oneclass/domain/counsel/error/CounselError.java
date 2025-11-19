package oneclass.oneclass.domain.counsel.error;


import oneclass.oneclass.global.exception.CustomError;

public enum CounselError implements CustomError {

    NOT_FOUND(404, "상담을 찾을 수 없습니다."),
    BAD_REQUEST(400, "잘못된 요청입니다."),
    MISSING_FIELD(400, "필수 입력값이 누락되었습니다."),
    INVALID_PARAM(400, "파라미터가 유효하지 않습니다."),
    CONFLICT(409, "동일한 상담이 여러 건 입니다."),
    
    // Validation 에러
    TITLE_REQUIRED(400, "상담 제목은 필수입니다."),
    TITLE_TOO_LONG(400, "상담 제목은 100자를 초과할 수 없습니다."),
    NAME_REQUIRED(400, "이름은 필수입니다."),
    NAME_TOO_LONG(400, "이름은 50자를 초과할 수 없습니다."),
    PHONE_REQUIRED(400, "전화번호는 필수입니다."),
    INVALID_PHONE_FORMAT(400, "전화번호는 10~11자리 숫자여야 합니다."),
    TYPE_REQUIRED(400, "상담 유형은 필수입니다."),
    TYPE_TOO_LONG(400, "상담 유형은 50자를 초과할 수 없습니다."),
    SUBJECT_TOO_LONG(400, "상담 주제는 100자를 초과할 수 없습니다."),
    DESCRIPTION_TOO_LONG(400, "상담 설명은 1000자를 초과할 수 없습니다."),
    INVALID_AGE(400, "나이는 0~150 사이여야 합니다."),
    INVALID_DATE_FORMAT(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd HH:mm)");

    private final int status;
    private final String message;

    CounselError(int status, String message) {
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


