package oneclass.oneclass.domain.member.error;


import oneclass.oneclass.global.exception.CustomError;

public enum MemberError implements CustomError {

    NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    UNAUTHORIZED(401, "비밀번호가 일치하지 않습니다."),
    FORBIDDEN(403,"조회 권한이 없습니다"),
    BAD_REQUEST(400,"잘못된 입력입니다"),

    //  회원가입 관련
    DUPLICATE_PHONE(409, "이미 사용 중인 전화번호입니다."),
    PASSWORD_TOO_SHORT(400, "비밀번호는 최소 8자 이상이어야 합니다."),
    PASSWORD_CONFIRM_MISMATCH(400, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    DUPLICATE_USERNAME(409,"이미 사용 중인 닉네임입니다."),
    DUPLICATE_EMAIL(409,"이미 사용 중인 이메일입니다."),

    //  필수값 누락
    USERNAME_REQUIRED(400, "이름이 필요합니다."),
    PHONE_REQUIRED(400, "전화번호가 필요합니다."),
    ROLE_REQUEST(400,"역할이 필요합니다"),
    PASSWORD_REQUEST(400,"비밀번호를 입력해주세요."),

    //  인증번호 관련
    VERIFICATION_CODE_REQUIRED(400,"인증코드가 필요합니다"),
    INVALID_VERIFICATION_CODE(400, "인증코드가 올바르지 않습니다."),
    EXPIRED_VERIFICATION_CODE(400, "인증코드가 만료되었습니다."),
    NOT_FOUND_VERIFICATION_CODE(404, "인증코드를 찾을 수 없습니다.");

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

