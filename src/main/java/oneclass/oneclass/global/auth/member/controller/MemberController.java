package oneclass.oneclass.global.auth.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.member.dto.*;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.global.auth.member.jwt.JwtProvider;
import oneclass.oneclass.global.auth.member.repository.RefreshTokenRepository;
import oneclass.oneclass.global.auth.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "회원 인증 API", description = "회원가입, 로그인, 비밀번호 찾기 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    @DeleteMapping("/parent/{parentId}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long parentId) {
        memberService.deleteParent(parentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add-students")
    public ResponseEntity<Void> addStudentsToParent(@RequestBody AddStudentsRequest request) {
        memberService.addStudentsToParent(
                request.getUsername(),
                request.getPassword(),
                request.getStudentId()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup-code")
    public void sendSignupVerificationCode(@RequestParam String academyCode) {
        memberService.sendSignupVerificationCode(academyCode);
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
    @PostMapping("/send-reset-password")
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
    public void logout(HttpServletRequest request, @RequestParam String username) {
        String token = jwtProvider.resolveToken(request);
        if (token == null) {
            throw new IllegalArgumentException("유효한 토큰이 필요합니다.");
        }
        if (!jwtProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        String tokenUsername = jwtProvider.getUsername(token);
        if (!username.equals(tokenUsername)) {
            throw new IllegalArgumentException("토큰과 요청 username이 일치하지 않습니다.");
        }
        // refreshToken이 DB에 존재하는지 체크
        if (!refreshTokenRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 로그아웃된 사용자입니다.");
        }

        memberService.logout(username);
    }
}