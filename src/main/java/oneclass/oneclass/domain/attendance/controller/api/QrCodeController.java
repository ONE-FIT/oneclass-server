package oneclass.oneclass.domain.attendance.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.mapper.QrCodeMapper;
import oneclass.oneclass.domain.attendance.dto.response.QrCodeResponse;
import oneclass.oneclass.domain.attendance.service.QrCodeService;
import oneclass.oneclass.domain.attendance.dto.response.BaseResponseData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
@Tag(name = "출석", description = "출석 API")
@SecurityRequirement(name = "BearerAuthentication")
public class QrCodeController extends QrCodeMapper {

    private final QrCodeService qrCodeService;

    @PostMapping("/code")
    @Operation(summary = "출석 코드 생성", description = "출석 코드 생성합니다")
    public BaseResponseData<QrCodeResponse> generateCheckCode() throws ExecutionException, InterruptedException {
        CompletableFuture<QrCodeResponse> codeResponseFuture = qrCodeService.generate();
        QrCodeResponse codes = codeResponseFuture.get();
        return BaseResponseData.created(
                "출석 코드 생성 성공",
                codes);
    }
}