package oneclass.oneclass.domain.academy.error;


import oneclass.oneclass.global.exception.CustomError;

public enum AcademyError implements CustomError {

    NOT_FOUND(404, "학원을 찾을 수 없습니다."),

    UNAUTHORIZED(401, "비밀번호가 일치하지 않습니다."),

    PASSWORD_MISMATCH(400, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),

    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(409, "이미 사용 중인 전화번호입니다."),

    INVALID_VERIFICATION_CODE(400, "인증코드가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(400, "인증코드가 만료되었습니다."),

    PASSWORD_TOO_SHORT(400, "비밀번호는 최소 8자 이상이어야 합니다."),

    INVALID_ACADEMY_CODE(400, "유효하지 않은 학원 코드입니다."),
    INVALID_ACADEMY_NAME(400, "유효하지 않은 학원 이름입니다."),

    VERIFICATION_CODE_NOT_FOUND(404, "인증코드를 찾을 수 없습니다."),

    TOKEN_EXPIRED(401, "토큰이 만료되었습니다."),
    TOKEN_INVALID(401, "유효하지 않은 토큰입니다."),

    ALREADY_LOGGED_OUT(400, "이미 로그아웃된 토큰입니다."),

    EMAIL_SEND_FAILED(500, "이메일 전송에 실패했습니다."),

    INVALID_EMAIL_FORMAT(400, "유효하지 않은 이메일 형식입니다."),
    INVALID_PHONE_FORMAT(400, "유효하지 않은 전화번호 형식입니다."),
    PASSWORD_WEAK(400, "비밀번호가 너무 약합니다. 특수문자, 숫자를 포함하세요."),

    INVALID_DATE_RANGE(400, "유효하지 않은 날짜 범위입니다.");

    private final int status;
    private final String message;

    AcademyError(int status, String message) {
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