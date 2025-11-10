package oneclass.oneclass.global.exception;

import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.error.AttendanceError;
import oneclass.oneclass.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.error(e));
    }

    // DTO 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        CustomException customException =
                new CustomException(CommonError.INVALID_INPUT_VALUE, errors);

        return ResponseEntity
                .status(customException.getStatus())
                .body(ApiResponse.error(customException));
    }

    // Enum 타입 불일치 등 @RequestParam / @PathVariable 타입 오류 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == AttendanceStatus.class) {
            CustomException customException =
                    new CustomException(AttendanceError.INVALID_STATUS, ex.getValue() + "는 올바른 출석 상태가 아닙니다.");
            return ResponseEntity
                    .status(customException.getStatus())
                    .body(ApiResponse.error(customException));
        }

        // 다른 타입 불일치라면 일반 BAD_REQUEST 처리
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(),
                "잘못된 요청 파라미터: " + ex.getValue()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorResponse));
    }
}
