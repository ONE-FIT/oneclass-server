package oneclass.oneclass.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.dto.request.*;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.error.MemberError;
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
    public ResponseEntity<ApiResponse<ResponseToken>> login(@RequestBody @Valid LoginRequest req) {
        String phone = normalizePhone(req.phone());
        ResponseToken token = memberService.login(phone, req.password());
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\D", "");
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

        // 2) JWE(5세그먼트)면 복호화
        if (isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt);
        }

        // 3) refresh 토큰 검증 (만료는 허용하고 삭제만 진행하고 싶다면 TOKEN_EXPIRED만 허용)
        try {
            jwtProvider.validateToken(rt);
        } catch (CustomException e) {
            if (!TokenError.TOKEN_EXPIRED.equals(e.getError())) throw e;
        }

        // 4) refresh 토큰 주체(phone)
        String phoneFromRefresh = jwtProvider.getPhone(rt);

        // 5) 인증 주체 phone 추출(중복 로직 -> 메서드화)
        String phoneFromAuth = resolveAuthenticatedPhone(authentication, request);

        // 6) 주체 일치 확인
        if (!phoneFromAuth.equals(phoneFromRefresh)) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }

        // 7) 해당 refresh 토큰 폐기
        memberService.logout(phoneFromRefresh, rt);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "계정탈퇴", description = "계정을 탈퇴합니다.")
    @DeleteMapping("/delete-user")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            Authentication authentication,
            HttpServletRequest request
    ){
        if (authentication == null) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
        String phoneFromAuth = resolveAuthenticatedPhone(authentication, request);
        memberService.deleteUser(phoneFromAuth);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "닉네임 생성", description = "닉네임을 생성합니다.")
    @PostMapping("/create-username")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> createUsername(@RequestParam String username) {
        memberService.createUsername(username);
        return ResponseEntity.ok(ApiResponse.success(null));
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

    @Operation(summary = "특정 선생님 맡고있는 학생 리스트조회", description = "학생을 조회합니다.")
    @GetMapping("/teachers/{teacherPhone}/students")
    @PreAuthorize("hasAnyRole('TEACHER','PARENT','STUDENT')")
    public ResponseEntity<ApiResponse<List<String>>> listStudentsOfTeacher(
            @PathVariable String teacherPhone,
            Authentication authentication
    ) {
        String requester = (authentication != null) ? authentication.getName() : null;
        List<String> students = memberService.listStudentsOfTeacher(requester, teacherPhone);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @Operation(summary = "특정 학생의 선생님 조회", description = "선생님을 조회합니다.")
    @GetMapping("/students/{studentPhone}/teachers")
    @PreAuthorize("hasAnyRole('TEACHER','PARENT','STUDENT')")
    public ResponseEntity<ApiResponse<List<String>>> listTeachersOfStudent(
            @PathVariable String studentPhone,
            Authentication authentication
    ) {
        String requester = (authentication != null) ? authentication.getName() : null;
        List<String> teachers = memberService.listTeachersOfStudent(requester, studentPhone);
        return ResponseEntity.ok(ApiResponse.success(teachers));
    }

    @Operation(summary = "부모님 삭제", description = "학생계정에 등록된 부모님을 삭제합니다.")
    @DeleteMapping("/parent/{parentId}")
    @PreAuthorize("hasAnyRole('PARENT','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteParent(@PathVariable Long parentId) {
        memberService.deleteParent(parentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "학생추가(부모님)", description = "부모님 계정에 자식을 추가합니다.")
    @PostMapping("/parent/add-students")
    @PreAuthorize("hasAnyRole('PARENT')")
    public ResponseEntity<ApiResponse<Void>> addStudentsToParent(@RequestBody @Valid AddStudentsRequest request) {
        memberService.addStudentsToParent(request.phone(), request.password(), request.studentPhones());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 인증 정보와 request attribute에서 phone(주체)을 일관되게 추출.
     * 1) 필터가 세팅한 request attribute "auth.phone" 우선
     * 2) Authentication.getName() 이 전화번호 패턴이면 그대로 사용
     * 3) 아니라면 username으로 간주하고 DB에서 Member 조회 후 phone 추출
     */
    private String resolveAuthenticatedPhone(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }

        String phoneFromAttr = (String) request.getAttribute("auth.phone");
        if (phoneFromAttr != null && !phoneFromAttr.isBlank()) {
            return phoneFromAttr;
        }

        String principal = authentication.getName();
        if (principal != null && principal.matches("^\\d{10,}$")) {
            return principal;
        }

        // principal이 username이라고 가정
        Member member = memberRepository.findByUsername(principal)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        return member.getPhone();
    }
}