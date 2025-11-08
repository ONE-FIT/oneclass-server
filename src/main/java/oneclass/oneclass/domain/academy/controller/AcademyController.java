package oneclass.oneclass.domain.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.academy.dto.request.AcademyLoginRequest;
import oneclass.oneclass.domain.academy.dto.request.AcademySignupRequest;
import oneclass.oneclass.domain.academy.dto.request.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.academy.dto.response.AcademySignupResponse;
import oneclass.oneclass.domain.academy.service.AcademyService;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.auth.jwt.TokenUtils;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/academy")
public class AcademyController {
    private final AcademyService academyService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입(학원)", description = "새로운 학원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<AcademySignupResponse> signup(@RequestBody @Valid AcademySignupRequest request) {
        AcademySignupResponse response = academyService.academySignup(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "학원계정으로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ResponseToken> login(@RequestBody @Valid AcademyLoginRequest request) {
        ResponseToken token = academyService.login(request.academyCode(), request.academyName(), request.password());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 인증코드를 발송합니다.")
    @PostMapping("/send-reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(@RequestBody @Valid ResetAcademyPasswordRequest request) {
        academyService.sendResetPasswordEmail(request.academyCode(), request.academyName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetAcademyPasswordRequest request) {
        academyService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그아웃", description = "Authorization(Access) 인증 후, 전달받은 Refresh 토큰만 폐기합니다. 헤더 X-Refresh-Token 사용.")
    @PostMapping("/logout")
    @PreAuthorize("hasRole('ACADEMY')")
    public ResponseEntity<Void> logout(
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
        return ResponseEntity.noContent().build();
    }
}