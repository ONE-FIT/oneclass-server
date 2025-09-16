// ErrorResponse.java
package oneclass.oneclass.global.exception;

public record ErrorResponse(String code, int status, String message) {

    public static ErrorResponse of(CustomException customException) {
        return new ErrorResponse(
                customException.getCode(),
                customException.getStatus(),
                customException.getMessage()
        );
    }

}
