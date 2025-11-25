package oneclass.oneclass.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.dto.request.*;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.member.service.MemberService;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.auth.jwt.TokenUtils;
import oneclass.oneclass.global.dto.ApiResponse;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원 인증 API", description = "회원가입, 로그인, 비밀번호 찾기 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Operation(summary = "회원가입 코드보내기(선생님)", description = "학원메일로 선생님 회원가입 코드를 보냅니다.")
    @PostMapping("/signup-code")
    public ResponseEntity<ApiResponse<Void>> sendSignupVerificationCode(@RequestParam String academyCode, @RequestParam String name) {
        memberService.sendSignupVerificationCode(academyCode, name);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "로그인", description = "회원 로그인 및 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResponseToken>> login(@RequestBody @Valid LoginRequest request) {
        // LoginRequest 전체를 서비스로 전달 (서비스가 내부에서 검증/정규화 처리)
        ResponseToken token = memberService.login(request);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @Operation(
            summary = "멤버 로그아웃",
            description = "Authorization(Access) 인증 후, 전달받은 Refresh 토큰만 폐기합니다. 헤더 X-Refresh-Token 사용."
    )
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest request,
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken
    ) {
        if (authentication == null) throw new CustomException(TokenError.UNAUTHORIZED);
        if (refreshToken == null || refreshToken.isBlank()) throw new CustomException(TokenError.UNAUTHORIZED);

        // 1) 입력 토큰 정리
        String rt = memberService.cleanupToken(refreshToken);

        // 2) JWE(5 세그먼트)면 복호화
        if (isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt);
        }

        // 3) refresh 토큰 검증 (만료는 허용하고 삭제만 진행하고 싶다면 TOKEN_EXPIRED만 허용)
        try {
            jwtProvider.validateToken(rt);
        } catch (CustomException e) {
            if (!TokenError.TOKEN_EXPIRED.equals(e.getError())) throw e;
        }

        // 4) refresh 토큰의 주체(subject = username)
        String usernameFromRefresh = jwtProvider.getUsername(rt);

        // 5) 인증 주체 username
        String usernameFromAuth = resolveAuthenticatedUsername(authentication, request);

        // 6) 주체 일치 확인
        if (!usernameFromAuth.equals(usernameFromRefresh)) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }

        // 7) 해당 refresh 토큰 폐기 (username 기반)
        memberService.logout(usernameFromRefresh, rt);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String resolveAuthenticatedUsername(Authentication authentication, HttpServletRequest request) {
        // 기본: SecurityContext의 principal 사용
        return authentication.getName();
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        memberService.resetPassword(
                request.phone(),
                request.newPassword(),
                request.checkPassword(),
                request.verificationCode()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private boolean isLikelyJwe(String t) {
        return TokenUtils.isLikelyJwe(t);
    }

    @Operation(summary = "선생님 계정에 학생 추가", description = "학생을 추가합니다.")
    @PostMapping("/teachers/{teacherPhone}/students")
    @PreAuthorize("hasAnyRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherStudentsResponse>> addStudentsToTeacher(
            @PathVariable String teacherPhone,
            @RequestBody @Valid TeacherStudentsRequest request,
            Authentication authentication
    ) {
        TeacherStudentsResponse response = memberService.addStudentsToTeacher(
                teacherPhone,
                request.studentPhones(),
                request.password()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "선생님 계정에 학생 제거", description = "학생을 제거합니다.")
    @DeleteMapping("/teachers/{teacherPhone}/students")
    @PreAuthorize("hasAnyRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> removeStudentsFromTeacher(
            @PathVariable String teacherPhone,
            @RequestBody @Valid TeacherStudentsRequest request,
            Authentication authentication
    ) {
        memberService.removeStudentsFromTeacher(teacherPhone, request.studentPhones());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "학생추가(부모님)", description = "부모님 계정에 자식을 추가합니다.")
    @PostMapping("/parent/add-students")
    @PreAuthorize("hasAnyRole('PARENT')")
    public ResponseEntity<ApiResponse<Void>> addStudentsToParent(@RequestBody @Valid AddStudentsRequest request) {
        memberService.addStudentsToParent(request.username(), request.password(), request.studentUsernames());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}