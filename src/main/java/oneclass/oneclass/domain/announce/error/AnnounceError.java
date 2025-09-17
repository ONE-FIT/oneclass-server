package oneclass.oneclass.domain.announce.error;

import oneclass.oneclass.global.exception.CustomError;

public enum AnnounceError implements CustomError {

    NOT_FOUND(404, "공지를 찾을 수 없습니다.");

    private final int status;
    private final String message;

    AnnounceError(int status, String message) {
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