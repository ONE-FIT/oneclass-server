package oneclass.oneclass.global.auth.member.error;

import oneclass.oneclass.global.exception.CustomError;

public enum UserError implements CustomError {

    NOT_FOUND(404, "유저를 찾을 수 없습니다.");

    private final int status;
    private final String message;

    UserError(int status, String message) {
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
