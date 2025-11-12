package oneclass.oneclass.domain.counsel.error;


import oneclass.oneclass.global.exception.CustomError;

public enum CounselError implements CustomError {

    NOT_FOUND(404, "상담을 찾을 수 없습니다."),
    BAD_REQUEST(400, "잘못된 요청입니다."),
    MISSING_FIELD(400, "필수 입력값이 누락되었습니다."),
    INVALID_PARAM(400, "파라미터가 유효하지 않습니다."),
    CONFLICT(409, "동일한 상담이 여러 건 입니다.");

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


