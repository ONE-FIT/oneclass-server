package oneclass.oneclass.global.dto;

import oneclass.oneclass.global.exception.CustomException;
import oneclass.oneclass.global.exception.ErrorResponse;

public record ApiResponse<T>(
        T data,
        ErrorResponse error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static ApiResponse<Void> error(CustomException customException) {
        return new ApiResponse<>(null, ErrorResponse.of(customException));
    }

    public static ApiResponse<Void> error(ErrorResponse error) {
        return new ApiResponse<>(null, error);
    }
}