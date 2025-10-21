package oneclass.oneclass.domain.counsel.error;


import oneclass.oneclass.global.exception.CustomError;

public enum CounselError implements CustomError {

    NOT_FOUND(404, "상담을 찾을 수 없습니다."),
    BAD_REQUEST(400,"잘못된 상태 전이입니다."),
    CONFLICT(409,"동일한 상담이 여러 건 입니다.");

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


