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
    public ResponseEntity<Void> sendSignupVerificationCode(@RequestParam String academyCode, @RequestParam String name) {
        memberService.sendSignupVerificationCode(academyCode, name);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "로그인", description = "회원 로그인 및 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<ResponseToken> login(@RequestBody LoginRequest req) {
        String phone = normalizePhone(req.getPhone());
        return ResponseEntity.ok(memberService.login(phone, req.getPassword()));
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        // 숫자만 남김: 하이픈/공백 제거
        return phone.replaceAll("\\D", "");
    }

    @Operation(
            summary = "멤버 로그아웃",
            description = "Authorization(Access) 인증 후, 전달받은 Refresh 토큰만 폐기합니다. 헤더 X-Refresh-Token 사용."
    )
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','TEACHER')")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletRequest request,
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken
    ) {
        if (authentication == null) throw new CustomException(TokenError.UNAUTHORIZED);
        if (refreshToken == null || refreshToken.isBlank()) throw new CustomException(TokenError.UNAUTHORIZED);

        // 1) 입력 토큰 정리 (Bearer 제거, 양끝 큰따옴표 제거)
        String rt = memberService.cleanupToken(refreshToken);

        // 2) JWE(5 세그먼트)면 복호화 → JWS
        if (isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt); // rt는 항상 String
        }

        // 3) refresh 토큰 검증 (만료면 DB 삭제만 하려면 EXPIRED 허용)
        try {
            jwtProvider.validateToken(rt);
        } catch (CustomException e) {
            if (!TokenError.TOKEN_EXPIRED.equals(e.getError())) throw e;
        }

        // 4) refresh 토큰의 subject(phone) 추출
        String phoneFromRefresh = jwtProvider.getPhone(rt);

        // 5) 인증 주체 phone 확보 (필터가 넣은 request attribute 우선)
        String phoneFromAuth = (String) request.getAttribute("auth.phone");
        if (phoneFromAuth == null || phoneFromAuth.isBlank()) {
            String principal = authentication.getName();
            if (principal != null && principal.matches("^\\d{10,}$")) {
                phoneFromAuth = principal; // principal이 phone인 경우
            } else {
                // principal이 username이면 phone 조회
                Member member = memberRepository.findByUsername(principal)
                        .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
                phoneFromAuth = member.getPhone();
            }
        }

        // 6) 주체 일치 확인
        if (!phoneFromAuth.equals(phoneFromRefresh)) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }

        // 7) 해당 refresh만 폐기
        memberService.logout(phoneFromRefresh, rt); // 둘 다 String

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "계정탈퇴", description = "계정을 탈퇴합니다.")
    @DeleteMapping("/delete-user")
    public ResponseEntity<Void> deleteUser(
            Authentication authentication,
            HttpServletRequest request
    ){
        if (authentication == null) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }

        String phoneFromAuth = (String) request.getAttribute("auth.phone");
        if (phoneFromAuth == null || phoneFromAuth.isBlank()) {
            String principal = authentication.getName();
            if (principal != null && principal.matches("^\\d{10,}$")) {
                phoneFromAuth = principal;
            } else {
                Member member = memberRepository.findByUsername(principal)
                        .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
                phoneFromAuth = member.getPhone();
            }
        }

        memberService.deleteUser(phoneFromAuth); // 서비스 레이어 시그니처도 변경 필요
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "닉네임 생성", description = "닉네임을 생성합니다.")
    @PostMapping("/create-username")
    public ResponseEntity<Void> createUsername(@RequestParam String username) {
        memberService.createUsername(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        memberService.resetPassword(
                request.getPhone(),
                request.getNewPassword(),
                request.getVerificationCode(),
                request.getCheckPassword()
        );
        return ResponseEntity.noContent().build();
    }

    private boolean isLikelyJwe(String t) {
        return TokenUtils.isLikelyJwe(t); // 5 segments
    }

    @Operation(summary = "선생님 계정에 학생 추가", description = "학생을 추가합니다.")
    @PostMapping("/teachers/{teacherPhone}/students")
    public ResponseEntity<TeacherStudentsResponse> addStudentsToTeacher(
            @PathVariable String teacherPhone,
            @RequestBody @Valid TeacherStudentsRequest request,
            Authentication authentication // 인증 정보(필요 시 요청자 확인)
    ) {
        TeacherStudentsResponse response = memberService.addStudentsToTeacher(
                teacherPhone,
                request.getPhone(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    // 선생님에게서 여러 학생 제거 (phone 기준)
    @Operation(summary = "선생님 계정에 학생 제거", description = "학생을 제거합니다.")
    @DeleteMapping("/teachers/{teacherPhone}/students")
    public ResponseEntity<Void> removeStudentsFromTeacher(
            @PathVariable String teacherPhone,
            @RequestBody @Valid TeacherStudentsRequest request,
            Authentication authentication
    ) {
        memberService.removeStudentsFromTeacher(teacherPhone, request.getPhone());
        return ResponseEntity.noContent().build();
    }

    // 선생님이 맡고 있는 학생 phone 리스트 조회 (phone 기준)
    @Operation(summary = "특정 선생님 맡고있는 학생 리스트조회", description = "학생을 조회합니다.")
    @GetMapping("/teachers/{teacherPhone}/students")
    public ResponseEntity<List<String>> listStudentsOfTeacher(
            @PathVariable String teacherPhone,
            Authentication authentication
    ) {
        String requester = (authentication != null) ? authentication.getName() : null;
        List<String> students = memberService.listStudentsOfTeacher(requester, teacherPhone);
        return ResponseEntity.ok(students);
    }

    // 특정 학생의 담당 선생님 phone 리스트 조회 (phone 기준)
    @Operation(summary = "특정 학생의 선생님 조회", description = "선생님을 조회합니다.")
    @GetMapping("/students/{studentPhone}/teachers")
    public ResponseEntity<List<String>> listTeachersOfStudent(
            @PathVariable String studentPhone,
            Authentication authentication
    ) {
        String requester = (authentication != null) ? authentication.getName() : null;
        List<String> teachers = memberService.listTeachersOfStudent(requester, studentPhone);
        return ResponseEntity.ok(teachers);
    }

    @Operation(summary = "부모님 삭제", description = "학생계정에 등록된 부모님을 삭제합니다.")
    @DeleteMapping("/parent/{parentId}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long parentId) {
        memberService.deleteParent(parentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "학생추가(부모님)", description = "부모님 계정에 자식을 추가합니다.")
    @PostMapping("/parent/add-students")
    public ResponseEntity<Void> addStudentsToParent(@RequestBody AddStudentsRequest request) {
        // 부모 식별자는 전화번호(request.getPhone()), 자녀 리스트도 전화번호 리스트(request.getStudentPhone())
        memberService.addStudentsToParent(request.getPhone(), request.getPassword(), request.getStudentPhone());
        return ResponseEntity.noContent().build();
    }
}