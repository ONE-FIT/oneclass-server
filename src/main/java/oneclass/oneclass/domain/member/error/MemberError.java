package oneclass.oneclass.domain.member.error;


import oneclass.oneclass.global.exception.CustomError;

public enum MemberError implements CustomError {

    NOT_FOUND(404, "아이디를 찾을 수 없습니다."),
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    BAD_REQUEST(400,"요청형식에 맞게 써주세요."),
    CONFLICT(409, "이미 사용중입니다."),
    NO_CONTENT(204,"존재하지 않는 코드입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다.") ;

    private final int status;
    private final String message;

    MemberError(int status, String message) {
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

