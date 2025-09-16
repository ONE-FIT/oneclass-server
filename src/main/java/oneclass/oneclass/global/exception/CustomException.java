// CustomException.java
package oneclass.oneclass.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final CustomError error;
    private final String code;
    private final int status;
    private final String message;

    public CustomException(CustomError error, String... args) {
        this.error = error;
        this.code = ((Enum<?>) error).name();
        this.status = error.getStatus();
        this.message = String.format(error.getMessage(), (Object[]) args);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
