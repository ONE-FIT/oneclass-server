package oneclass.oneclass.domain.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.academy.dto.AcademyLoginRequest;
import oneclass.oneclass.domain.academy.dto.MadeRequest;
import oneclass.oneclass.domain.academy.dto.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.academy.service.AcademyService;
import oneclass.oneclass.domain.member.dto.ResponseToken;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/academy")
public class AcademyController {
    private final AcademyService academyService;
    private final JwtProvider jwtProvider;
    //학원 계정 만들기
    @Operation(summary = "회원가입(학원)", description = "새로운 학원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Void> made(@RequestBody MadeRequest request) {
        academyService.madeAcademy(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @Operation(summary = "로그인", description = "학원계정으로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ResponseToken> login(@RequestBody AcademyLoginRequest request) {
        ResponseToken token = academyService.login(request.getAcademyCode(), request.getAcademyName(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    // 인증코드 발송
    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 인증코드를 발송합니다.")
    @PostMapping("/send-reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(@RequestBody ResetAcademyPasswordRequest request) {
        academyService.sendResetPasswordEmail(request.getAcademyCode(), request.getAcademyName());
        return ResponseEntity.ok().build();
    }

    // 비밀번호 초기화
    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetAcademyPasswordRequest request) {
        academyService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "로그아웃", description = "학원계정을 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        String token = jwtProvider.resolveToken(request);
        if (token == null) {
            throw new CustomException(TokenError.UNAUTHORIZED); // 혹은 TokenError.UNAUTHORIZED 로 교체
        }

        // 토큰 유효성 검증
        jwtProvider.validateToken(token);

        // username 추출
        String academyCode = jwtProvider.getUsername(token);

        // 서비스 호출 (RefreshToken 삭제 / 예외 처리 내장)
        academyService.logout(academyCode);

        return ResponseEntity.noContent().build(); // 204
    }
}
