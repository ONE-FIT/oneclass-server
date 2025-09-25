package oneclass.oneclass.global.auth.academy.error;


import oneclass.oneclass.global.exception.CustomError;

public enum AuthError implements CustomError {

    NOT_FOUND(404, "학원코드를 찾을 수 없습니다."),
    UNAUTHORIZED(401, "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(400, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");//회원가입할 때

    private final int status;
    private final String message;

    AuthError(int status, String message) {
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
