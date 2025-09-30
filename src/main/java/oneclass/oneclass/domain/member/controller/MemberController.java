package oneclass.oneclass.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.dto.*;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.domain.member.repository.RefreshTokenRepository;
import oneclass.oneclass.domain.member.service.MemberService;
import oneclass.oneclass.global.auth.CustomUserDetails;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "회원 인증 API", description = "회원가입, 로그인, 비밀번호 찾기 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Operation(summary = "부모님 삭제", description = "학생계정에 등록된 부모님을 삭제합니다.")
    @DeleteMapping("/parent/{parentId}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long parentId) {
        memberService.deleteParent(parentId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "학생추가(부모님)", description = "부모님 계정에 자식을 추가합니다.")
    @PostMapping("/add-students")
    public ResponseEntity<Void> addStudentsToParent(@RequestBody AddStudentsRequest request) {
        memberService.addStudentsToParent(
                request.getUsername(),
                request.getPassword(),
                request.getStudentId()
        );
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "회원가입 코드보내기(선생님)", description = "학원메일로 선생님 회원가입 코드를 보냅니다.")
    @PostMapping("/signup-code")
    public void sendSignupVerificationCode(@RequestParam String academyCode , String username) {
        memberService.sendSignupVerificationCode(academyCode , username);
    }

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
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
        memberService.logout(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}