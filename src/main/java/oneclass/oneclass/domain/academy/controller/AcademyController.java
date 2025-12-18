package oneclass.oneclass.domain.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.academy.dto.request.AcademyLoginRequest;
import oneclass.oneclass.domain.academy.dto.request.AcademySignupRequest;
import oneclass.oneclass.domain.academy.dto.request.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.academy.dto.request.SendResetPasswordRequest;
import oneclass.oneclass.domain.academy.dto.response.AcademySignupResponse;
import oneclass.oneclass.domain.academy.dto.response.PendingAcademyResponse;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.service.AcademyService;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.auth.jwt.TokenUtils;
import oneclass.oneclass.global.dto.ApiResponse;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/academy")
public class AcademyController {
    private final AcademyService academyService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입(학원)", description = "새로운 학원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AcademySignupResponse>> signup(@RequestBody @Valid AcademySignupRequest request) {
        AcademySignupResponse response = academyService.academySignup(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그인", description = "학원계정으로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResponseToken>> login(@RequestBody @Valid AcademyLoginRequest request) {
        ResponseToken token = academyService.login(request.academyCode(), request.academyName(), request.password());
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @Operation(summary = "학원 승인", description = "승인되지 않은 학원을 승인합니다.")
    @PostMapping("/{code}/approve")
    public ResponseEntity<ApiResponse<Void>> approveAcademy(@PathVariable String code, Principal principal, HttpServletRequest req) {
        log.info("Approve requested by user={} code={} UA={}", principal.getName(), code, req.getHeader("User-Agent"));
        academyService.approveAcademy(principal.getName(), code);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 인증코드를 발송합니다.")
    @PostMapping("/send-reset-password")
    public ResponseEntity<ApiResponse<Void>> sendResetPasswordEmail(@RequestBody @Valid SendResetPasswordRequest request) {
        academyService.sendResetPasswordEmail(request.academyCode(),request.academyName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetAcademyPasswordRequest request) {
        academyService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "로그아웃", description = "Authorization(Access) 인증 후, 전달받은 Refresh 토큰만 폐기합니다. 헤더 X-Refresh-Token 사용.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken
    ) {
        if (authentication == null) throw new CustomException(TokenError.UNAUTHORIZED);
        if (refreshToken == null || refreshToken.isBlank()) throw new CustomException(TokenError.UNAUTHORIZED);

        // 1) 헤더 정리 + JWE 대응
        String rt = TokenUtils.cleanup(refreshToken);
        if (TokenUtils.isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt);
        }

        // 2) 유효성 검증
        jwtProvider.validateToken(rt);

        // 3) 주체 일치(Access 주체 == Refresh 주체)
        String academyCode = authentication.getName();
        String subject = jwtProvider.getUsername(rt);
        if (!academyCode.equals(subject)) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }

        // 4) 해당 Refresh만 폐기
        academyService.logout(academyCode, rt);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비승인 학원 조회",description = "승인되지 않은 학원들을 조회합니다.")
    @GetMapping("/pending-academies")
    public ResponseEntity<ApiResponse<List<PendingAcademyResponse>>> pendingAcademies() {
        List<PendingAcademyResponse> pendingAcademies = academyService.getPendingAcademies();
        return ResponseEntity.ok(ApiResponse.success(pendingAcademies));
    }
}