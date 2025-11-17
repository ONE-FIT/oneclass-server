package oneclass.oneclass.domain.member.error;

import oneclass.oneclass.global.exception.CustomError;

public enum TokenError implements CustomError {

    NOT_FOUND(404, "토큰을 찾을 수 없습니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    UNAUTHORIZED(401,"잘못된 토큰입니다."),
    INTERNAL_SERVER_ERROR(500,"암호화에 실패하였습니다."),
    BAD_REQUEST(400,"요청형식에 맞게 써주세요."),
    INVALID_TOKEN(400, "유효하지 않은 토큰입니다.");

    private final int status;
    private final String message;

    TokenError(int status, String message) {
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
