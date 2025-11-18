package oneclass.oneclass.domain.announce.error;

import oneclass.oneclass.global.exception.CustomError;

public enum AnnounceError implements CustomError {

    NOT_FOUND(404, "공지를 찾을 수 없습니다."),
    ILLEGAL_TIME(400, "시간 형식이 맞지 않습니다."),
    PAST_TIME(400, "예약 시간은 현재 시간으로부터 최소 30분 이후여야 합니다.");


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