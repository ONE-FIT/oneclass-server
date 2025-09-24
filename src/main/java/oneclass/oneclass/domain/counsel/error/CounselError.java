package oneclass.oneclass.domain.counsel.error;


import oneclass.oneclass.global.exception.CustomError;

public enum CounselError implements CustomError {

    NOT_FOUND(404, "상담을 찾을 수 없습니다.");

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


