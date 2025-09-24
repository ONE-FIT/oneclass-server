package oneclass.oneclass.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonError implements CustomError {
    INVALID_INPUT_VALUE(400, "유효하지 않은 입력입니다.");

    private final int status;
    private final String message;
}