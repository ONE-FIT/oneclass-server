package oneclass.oneclass.domain.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.ResponseToken;
import oneclass.oneclass.domain.member.dto.SignupRequest;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.entity.RefreshToken;
import oneclass.oneclass.domain.member.entity.Role;
import oneclass.oneclass.domain.member.entity.VerificationCode;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.member.repository.RefreshTokenRepository;
import oneclass.oneclass.domain.member.repository.VerificationCodeRepository;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AcademyRepository academyRepository;
    private final AcademyVerificationCodeRepository academyVerificationCodeRepository;
    private final JavaMailSender javaMailSender;

    // 회원가입
    @Override
    public void signup(SignupRequest request) {
        Role selectRole = request.getRole();
        if (selectRole == null) throw new CustomException(MemberError.BAD_REQUEST);

        // 비밀번호 확인
        if (request.getPassword() == null || request.getCheckPassword() == null
                || !request.getPassword().equals(request.getCheckPassword())) {
            throw new CustomException(MemberError.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다.");
        }

        // username 중복
        if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new CustomException(MemberError.CONFLICT);
        }

        // 이메일/전화 중복
        validatePhoneDuplication(request.getPhone());

        // 역할별 처리
        switch (selectRole) {
            case TEACHER -> signupTeacher(request);
            case STUDENT -> signupStudent(request);
            case PARENT  -> signupParent(request);
            default      -> throw new CustomException(MemberError.BAD_REQUEST);
        }
    }

    // 로그인(토큰 발급/회전)
    @Override
    public ResponseToken login(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(MemberError.INVALID_PASSWORD);
        }

        String roleClaim = "ROLE_" + member.getRole().name();

        RefreshToken refresh = refreshTokenRepository.findByUsername(username).orElse(null);

        String accessToken;
        String refreshTokenString;

        if (refresh != null) {
            if (!refresh.isExpired()) {
                // 기존 refresh 유지 + access 재발급
                accessToken = jwtProvider.generateAccessToken(username, roleClaim);
                refreshTokenString = refresh.getToken();
            } else {
                // refresh 만료 → 새 쌍 발급(회전)
                ResponseToken pair = jwtProvider.generateToken(username, roleClaim);
                refresh.rotate(pair.getRefreshToken(), LocalDateTime.now().plusDays(28));
                accessToken = pair.getAccessToken();
                refreshTokenString = pair.getRefreshToken();
            }
        } else {
            // 최초 발급
            ResponseToken pair = jwtProvider.generateToken(username, roleClaim);
            RefreshToken newRt = RefreshToken.builder()
                    .username(username)
                    .token(pair.getRefreshToken())
                    .expiryDate(LocalDateTime.now().plusDays(28))
                    .build();
            refreshTokenRepository.save(newRt);

            accessToken = pair.getAccessToken();
            refreshTokenString = pair.getRefreshToken();
        }

        return new ResponseToken(accessToken, refreshTokenString);
    }

    // 아이디 찾기
    @Override
    public String findUsername(String phone) {
        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        return member.getUsername();
    }

//    // 비번 재설정 이메일 발송 <--- 이거를 전화번호로 보내게 바꿔야됨
//    @Override
//    public void sendResetPasswordEmail(String phone) {
//        var member = memberRepository.findByPhone(phone)
//                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
//        String username = member.getUsername();
//
//        String tempCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
//
//        verificationCodeRepository.save(
//                VerificationCode.builder()
//                        .usernameOrEmail(username)
//                        .code(tempCode) // 대문자로 저장
//                        .expiry(LocalDateTime.now().plusMinutes(5))
//                        .build()
//        );
//
//        emailService.sendSimpleMail(member.getEmail(), "비밀번호 재설정", "인증코드: " + tempCode);
//    }

    // 비번 재설정
    @Override
    public void resetPassword(String username, String newPassword, String checkPassword, String verificationCode) {
        if (newPassword == null || !newPassword.equals(checkPassword)) {
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        }
        if (username == null || username.isBlank()) {
            throw new CustomException(MemberError.USERNAME_REQUIRED);
        }
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new CustomException(MemberError.VERIFICATION_CODE_REQUIRED);
        }

        String provided = normalizeCode(verificationCode);

        var codeEntry = verificationCodeRepository.findById(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND_VERIFICATION_CODE));

        String saved = normalizeCode(codeEntry.getCode());

        if (!saved.equals(provided)) {
            throw new CustomException(MemberError.INVALID_VERIFICATION_CODE, "인증코드가 일치하지 않습니다.");
        }
        if (codeEntry.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(MemberError.TOKEN_EXPIRED, "인증코드가 만료되었습니다.");
        }

        // 1회용 코드 삭제
        verificationCodeRepository.deleteById(username);

        var member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    private String normalizeCode(String code) {
        if (code == null) return "";
        return code.trim().replaceAll("\\s+", "").toUpperCase();
    }

    // 로그아웃: 특정 refresh 토큰만 폐기
    @Override
    public void logout(String username, String refreshToken) {
        // 호출 전 컨트롤러에서 유효성/주체 일치 검증을 수행
        boolean exists = refreshTokenRepository.existsByUsernameAndToken(username, refreshToken);
        if (!exists) {
            throw new CustomException(TokenError.UNAUTHORIZED); // 이미 폐기되었거나 불일치
        }
        refreshTokenRepository.deleteByUsernameAndToken(username, refreshToken);
    }

    // 회원가입 코드(선생님)
    @Override
    public void sendSignupVerificationCode(String academyCode, String name) {
        if (academyCode == null) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        String email = academy.getEmail();
        String academyName = academy.getAcademyName();

        String tempCode = UUID.randomUUID().toString().substring(0, 13);
        AcademyVerificationCode verificationCode = AcademyVerificationCode.builder()
                .academyCode(academyCode)
                .code(tempCode)
                .expiry(LocalDateTime.now().plusMinutes(10))
                .build();
        academyVerificationCodeRepository.save(verificationCode);

        String subject = "회원가입 인증코드 안내";
        String text = String.format(
                "%s님이 %s 학원으로 가입하려고 합니다.%n아래 인증코드를 10분 내에 입력해주세요.%n%n인증코드: %s",
                name, academyName, tempCode
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    // 부모-자식: 자녀 추가(부모님)
    @Override
    public void addStudentsToParent(String username, String password, List<String> studentUsernames) {
        Member parent = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        if (!passwordEncoder.matches(password, parent.getPassword())) {
            throw new CustomException(MemberError.INVALID_PASSWORD);
        }
        if (studentUsernames == null || studentUsernames.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "자녀 username 목록이 필요합니다.");
        }

        for (String su : studentUsernames) {
            Member child = memberRepository.findByUsername(su)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + su));
            parent.addParentStudent(child); // ManyToMany(username FK) 양방향 연결
        }
        memberRepository.save(parent);
    }

    // 부모님 삭제
    @Override
    public void deleteParent(Long parentId) {
        Member parent = memberRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        parent.getParentStudents().clear();
        memberRepository.save(parent);
        memberRepository.delete(parent);
    }

    // 리프레시 토큰으로 액세스 재발급
    @Override
    public ResponseToken reissue(String refreshToken) {
        // 1) 문자열 정리
        String rt = cleanupToken(refreshToken);

        // 2) JWE(5 세그먼트)면 복호화
        if (isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt);
        }

        // 3) 유효성 검사
        jwtProvider.validateToken(rt);

        // 4) 주체(username) 확인
        String username = jwtProvider.getUsername(rt);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        // 5) DB에 저장된 refresh와 일치/만료 검증
        RefreshToken saved = refreshTokenRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(TokenError.UNAUTHORIZED));
        if (!saved.getToken().equals(rt) || saved.isExpired()) {
            throw new CustomException(TokenError.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }

        // 6) 새 access 발급(리프레시는 유지)
        String roleClaim = "ROLE_" + member.getRole().name();
        String newAccessToken = jwtProvider.generateAccessToken(username, roleClaim);

        return new ResponseToken(newAccessToken, rt);
    }

    /* ==================== 내부 유틸/역할별 회원가입 ==================== */

    private void signupTeacher(SignupRequest request) {
        String academyCode = request.getAcademyCode();
        String userInputCode = request.getVerificationCode();

        if (academyCode == null || academyCode.trim().isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        if (userInputCode == null || userInputCode.trim().isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        AcademyVerificationCode savedCode = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!savedCode.getCode().equals(userInputCode)) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        if (savedCode.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(MemberError.TOKEN_EXPIRED);
        }
        academyVerificationCodeRepository.delete(savedCode);

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .academy(academy)
                .name(request.getName())
                .phone(request.getPhone())
//                .email(request.getEmail())
                .build();

        memberRepository.save(member);
    }

    private void signupStudent(SignupRequest request) {
        String academyCode = request.getAcademyCode();
        if (academyCode == null || academyCode.trim().isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .academy(academy)
                .name(request.getName())
                .phone(request.getPhone())
//                .email(request.getEmail())
                .build();

        memberRepository.save(member);
    }

    private void signupParent(SignupRequest request) {
        List<String> studentUsernames = request.getStudentUsername(); // studentId → studentUsername
        if (studentUsernames == null || studentUsernames.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }

        List<Member> children = new ArrayList<>();
        for (String s : studentUsernames) {
            Member child = memberRepository.findByUsername(s)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
            children.add(child);
        }

        Member parent = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .name(request.getName())
                .phone(request.getPhone())
//                .email(request.getEmail())
                .parentStudents(children)
                .build();

        memberRepository.save(parent);
    }

    private void validatePhoneDuplication(String phone) {
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new CustomException(MemberError.CONFLICT, "이미 사용중인 이메일 또는 전화번호입니다.");
        }
    }

    private boolean isLikelyJwe(String t) {
        if (t == null) return false;
        int dots = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dots++;
        return dots == 4; // 5 segments(JWE)
    }
    @SuppressWarnings("DuplicatedCode")
    private String cleanupToken(String token) {
        if (token == null) return null;
        String v = token.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) v = v.substring(7).trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        return v;
    }
    @Override
    public void addStudentsToTeacher(String teacherUsername, List<String> studentUsernames, String password) {
        // 인증된 사용자 확인
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("인증이 필요합니다.");
        }
        String requester = auth.getName();

        // 요청자가 해당 교사 본인이거나 ADMIN 권한을 가지고 있어야 함
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!requester.equals(teacherUsername) && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("해당 교사의 학생을 수정할 권한이 없습니다.");
        }

        // 기존 유효성 검사
        if (teacherUsername == null || teacherUsername.isBlank() || studentUsernames == null || studentUsernames.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "교사/학생 정보가 필요합니다.");
        }
        if (password == null || password.isBlank()) {
            throw new CustomException(MemberError.BAD_REQUEST, "비밀번호가 필요합니다.");
        }

        Member teacher = memberRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, teacher.getPassword()) && !isAdmin) {
            // 관리자면 비밀번호 체크를 건너뛸 수 있게 했음(선택적)
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH, "비밀번호가 올바르지 않습니다.");
        }

        if (teacher.getRole() != Role.TEACHER) {
            throw new CustomException(MemberError.BAD_REQUEST, "해당 사용자는 선생님이 아닙니다.");
        }

        for (String s : studentUsernames) {
            Member student = memberRepository.findByUsername(s)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + s));
            if (student.getRole() != Role.STUDENT) {
                throw new CustomException(MemberError.BAD_REQUEST, "학생이 아닌 계정입니다: " + s);
            }
            teacher.addStudent(student); // 양방향 연결
        }
        memberRepository.save(teacher); // owning side(teacher) 저장 → join table 반영
    }

    @Override
    public void removeStudentsFromTeacher(String teacherUsername, List<String> studentUsernames) {
        // 인증된 사용자 확인
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("인증이 필요합니다.");
        }
        String requester = auth.getName();

        // 요청자가 해당 교사 본인이거나 ADMIN 권한을 가지고 있어야 함
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!requester.equals(teacherUsername) && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("해당 교사의 학생을 수정할 권한이 없습니다.");
        }

        if (teacherUsername == null || teacherUsername.isBlank() || studentUsernames == null || studentUsernames.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "교사/학생 정보가 필요합니다.");
        }

        Member teacher = memberRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));
        if (teacher.getRole() != Role.TEACHER) {
            throw new CustomException(MemberError.BAD_REQUEST, "해당 사용자는 선생님이 아닙니다.");
        }

        for (String s : studentUsernames) {
            Member student = memberRepository.findByUsername(s)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + s));
            teacher.removeStudent(student); // 양방향 해제
        }
        memberRepository.save(teacher);
    }

    @Override
    public List<String> listStudentsOfTeacher(String requesterUsername, String teacherUsername) {
        // teacher 조회(연관관계 포함)
        Member teacher = memberRepository.findWithRelationsByUsername(teacherUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));

        Member requester = null;
        if (requesterUsername != null) {
            requester = memberRepository.findWithRelationsByUsername(requesterUsername)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "요청자 정보를 찾을 수 없습니다."));
        }

        Set<String> teacherStudents = teacher.getTeachingStudents().stream()
                .map(Member::getUsername).collect(Collectors.toSet());

        // 인증 없이 요청한 경우(anonymous) : 허용하지 않음
        if (requester == null) {
            throw new CustomException(MemberError.FORBIDDEN);
        }

        // ADMIN 또는 본인(teacher)인 경우 전체 반환
        if (requester.getRole() == Role.TEACHER || requester.getUsername().equals(teacherUsername)) {
            return teacherStudents.stream().sorted().collect(Collectors.toList());
        }

        // 부모인 경우: 부모의 자녀와 teacherStudents의 교집합만 반환
        if (requester.getRole() == Role.PARENT) {
            Set<String> parentChildren = requester.getParentStudents().stream()
                    .map(Member::getUsername).collect(Collectors.toSet());
            return parentChildren.stream()
                    .filter(teacherStudents::contains)
                    .sorted()
                    .collect(Collectors.toList());
        }

        // 학생 본인이나 그 외 역할은 허용하지 않음
        throw new CustomException(MemberError.FORBIDDEN);
    }


    @Override
    public List<String> listTeachersOfStudent(String requesterUsername, String studentUsername) {
        Member student = memberRepository.findWithRelationsByUsername(studentUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다."));

        Member requester = null;
        if (requesterUsername != null) {
            requester = memberRepository.findWithRelationsByUsername(requesterUsername)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "요청자 정보를 찾을 수 없습니다."));
        }

        Set<String> studentTeachers = student.getTeachers().stream()
                .map(Member::getUsername).collect(Collectors.toSet());

        if (requester == null) {
            throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다. 로그인 후 시도하세요.");
        }

        switch (requester.getRole()) {
            case STUDENT:
                if (requester.getUsername().equals(studentUsername)) {
                    return studentTeachers.stream().sorted().collect(Collectors.toList());
                }
                break;

            case PARENT:
                // requester.getUsername()를 final 로컬 변수에 복사
                final String requesterName = requester.getUsername();
                boolean isParentOf = student.getParents().stream()
                        .anyMatch(p -> p.getUsername().equals(requesterName));
                if (isParentOf) {
                    return studentTeachers.stream().sorted().collect(Collectors.toList());
                }
                break;

            case TEACHER:
                // 요청자가 그 학생의 담당 교사 목록에 포함되어 있으면 허용
                if (studentTeachers.contains(requester.getUsername())) {
                    return studentTeachers.stream().sorted().collect(Collectors.toList());
                }
                break;

            default:
                break;
        }

        throw new CustomException(MemberError.FORBIDDEN);
    }
}