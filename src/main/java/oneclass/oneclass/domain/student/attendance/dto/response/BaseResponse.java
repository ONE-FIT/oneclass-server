package oneclass.oneclass.domain.student.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class BaseResponse {

    private int status;
    private String message;

//    public static BaseResponse of(HttpStatus status, String message) {
//        return new BaseResponse(status.value(), message);
//    }  <---이건 큐빅에서 있던 코드인데 큐빅에서도 주석처리 되어 있고 사용 위치도 없긴 함

    public static BaseResponse ok(String message) {
        return new BaseResponse(HttpStatus.OK.value(), message);
    }

    public static BaseResponse created(String message) {
        return new BaseResponse(HttpStatus.CREATED.value(), message);
    }

}
