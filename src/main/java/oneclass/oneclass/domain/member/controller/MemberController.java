package oneclass.oneclass.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.dto.*;
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
import java.util.Map;

@Tag(name = "회원 인증 API", description = "회원가입, 로그인, 비밀번호 찾기 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Operation(summary = "부모님 삭제", description = "학생계정에 등록된 부모님을 삭제합니다.")
    @DeleteMapping("/parent/{parentId}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long parentId) {
        memberService.deleteParent(parentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "학생추가(부모님)", description = "부모님 계정에 자식을 추가합니다.")
    @PostMapping("/add-students")
    public ResponseEntity<Void> addStudentsToParent(@RequestBody AddStudentsRequest request) {
        memberService.addStudentsToParent(request.getUsername(), request.getPassword(), request.getStudentUsernames());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원가입 코드보내기(선생님)", description = "학원메일로 선생님 회원가입 코드를 보냅니다.")
    @PostMapping("/signup-code")
    public void sendSignupVerificationCode(@RequestParam String academyCode, @RequestParam String name) {
        memberService.sendSignupVerificationCode(academyCode, name);
    }

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/signup")
    public void signup(@RequestBody SignupRequest request) {
        memberService.signup(request);
    }

    @Operation(summary = "로그인", description = "회원 로그인 및 토큰 발급")
    @PostMapping("/login")
    public ResponseToken login(@RequestBody LoginRequest request) {
        return memberService.login(request.getName(), request.getPassword());
    }

    @Operation(
            summary = "멤버 로그아웃",
            description = "Authorization(Access) 인증 후, 전달받은 Refresh 토큰만 폐기합니다. 헤더 X-Refresh-Token 사용."
    )
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('STUDENT','PARENT','TEACHER')")
    public ResponseEntity<Void> logoutMember(
            Authentication authentication,
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken
    ) {
        if (authentication == null) throw new CustomException(TokenError.UNAUTHORIZED);
        if (refreshToken == null || refreshToken.isBlank()) throw new CustomException(TokenError.UNAUTHORIZED);

        String rt = cleanToken(refreshToken);

        // JWE(5 세그먼트)면 복호화 후 JWS 를 얻음
        if (isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt);
        }

        // 1) Refresh 토큰 유효성/서명/만료 검증
        jwtProvider.validateToken(rt);

        // 2) Refresh subject와 인증 주체(username) 일치 확인
        String username = authentication.getName();
        String subject = jwtProvider.getUsername(rt);
        if (!username.equals(subject)) throw new CustomException(TokenError.UNAUTHORIZED);

        // 3) 해당 Refresh 토큰만 폐기
        memberService.logout(username, rt);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "아이디 찾기", description = "이메일 또는 전화번호로 아이디를 조회합니다.")
    @GetMapping("/find-username")
    public String findUsername(@RequestParam String phone) {
        return memberService.findUsername(phone);
    }

    @PostMapping("/create-username")
    public void createUsername(@RequestParam String username) {
        memberService.createUsername(username);
    }

//    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정 인증코드를 발송합니다.")
//    @PostMapping("/send-reset-password-email")
//    public void sendResetPasswordEmail(@RequestBody Map<String, String> request) {
//        String phone = request.get("phone");
//        memberService.sendResetPasswordEmail(phone);
//    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        memberService.resetPassword(
                request.getUsername(),
                request.getNewPassword(),
                request.getVerificationCode(),
                request.getCheckPassword()
        );
    }

    private String cleanToken(String t) {
        String v = t.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) v = v.substring(7).trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
        return v;
    }

    private boolean isLikelyJwe(String t) {
        return TokenUtils.isLikelyJwe(t); // 5 segments
    }

    //선생님한테 배우는 학생 추가
    @Operation(summary = "선생님 계정에 학생 추가", description = "학생을 추가합니다.")
    @PostMapping("/teachers/{teacherUsername}/students")
    public ResponseEntity<Void> addStudentsToTeacher(
            @PathVariable String teacherUsername,
            @RequestBody @Valid TeacherStudentsRequest request,
            Authentication authentication // 인증 정보(필요 시 요청자 확인)
    ) {
        // 현재 서비스 시그니처는 teacherUsername, studentUsernames, password 를 받음.
        // 요청자가 본인(teacher)인지 검증하려면 authentication을 사용해 토큰 기반 확인을 추가 구현 가능.
        memberService.addStudentsToTeacher(teacherUsername, request.getStudentUsernames(), request.getPassword());
        return ResponseEntity.noContent().build();
    }

    // 선생님에게서 여러 학생 제거
    @DeleteMapping("/teachers/{teacherUsername}/students")
    public ResponseEntity<Void> removeStudentsFromTeacher(
            @PathVariable String teacherUsername,
            @RequestBody @Valid TeacherStudentsRequest request,
            Authentication authentication
    ) {
        // remove에서는 password가 현재 필요없음(서비스 레벨에서 role 체크)
        memberService.removeStudentsFromTeacher(teacherUsername, request.getStudentUsernames());
        return ResponseEntity.noContent().build();
    }

    // 선생님이 맡고 있는 학생 username 리스트 조회
    @GetMapping("/teachers/{teacherUsername}/students")
    public ResponseEntity<List<String>> listStudentsOfTeacher(
            @PathVariable String teacherUsername,
            Authentication authentication
    ) {
        String requester = (authentication != null) ? authentication.getName() : null;
        List<String> students = memberService.listStudentsOfTeacher(requester, teacherUsername);
        return ResponseEntity.ok(students);
    }

    // 특정 학생의 담당 선생님 username 리스트 조회
    @GetMapping("/students/{studentUsername}/teachers")
    public ResponseEntity<List<String>> listTeachersOfStudent(
            @PathVariable String studentUsername,
            Authentication authentication
    ) {
        String requester = (authentication != null) ? authentication.getName() : null;
        List<String> teachers = memberService.listTeachersOfStudent(requester, studentUsername);
        return ResponseEntity.ok(teachers);
    }
}