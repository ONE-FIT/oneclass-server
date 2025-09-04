package oneclass.oneclass.global.auth.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.member.dto.LoginRequest;
import oneclass.oneclass.global.auth.member.dto.ResetPasswordRequest;
import oneclass.oneclass.global.auth.member.dto.ResponseToken;
import oneclass.oneclass.global.auth.member.dto.SignupRequest;
import oneclass.oneclass.global.auth.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "회원 인증 API", description = "회원가입, 로그인, 비밀번호 찾기 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/signup")
    public void signup(@RequestBody SignupRequest request) {
        memberService.signup(request);
    }

    @Operation(summary = "로그인", description = "회원 로그인 및 토큰 발급")
    @PostMapping("/login")
    public ResponseToken login(@RequestBody LoginRequest request) {
        return memberService.login(request.getUsername(), request.getPassword());
    }

    @Operation(summary = "아이디 찾기", description = "이메일 또는 전화번호로 아이디를 조회합니다.")
    @GetMapping("/find-username")
    public String findUsername(@RequestParam String emailOrPhone) {
        return memberService.findUsername(emailOrPhone);
    }

    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 인증코드를 발송합니다.")
    @PostMapping("/send-reset-password-email")
    public void sendResetPasswordEmail(@RequestBody Map<String, String> request) {
        String emailOrPhone = request.get("emailOrPhone");
        memberService.sendResetPasswordEmail(emailOrPhone);
    }
    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        memberService.resetPassword(
                request.getUsername(),
                request.getNewPassword(),
                request.getVerificationCode()
        );
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 폐기하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public void logout(@RequestParam String username) {
        memberService.logout(username);
    }
}