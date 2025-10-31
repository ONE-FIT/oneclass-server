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
//import oneclass.oneclass.domain.member.entity.VerificationCode;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.member.repository.RefreshTokenRepository;
import oneclass.oneclass.domain.member.repository.VerificationCodeRepository;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
//    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AcademyRepository academyRepository;
    private final AcademyVerificationCodeRepository academyVerificationCodeRepository;
//    private final JavaMailSender javaMailSender;

    @Override
    public void signup(SignupRequest request) {
        Role selectRole = request.getRole();
        if (selectRole == null) throw new CustomException(MemberError.BAD_REQUEST);

        if (request.getPassword() == null || request.getCheckPassword() == null
                || !request.getPassword().equals(request.getCheckPassword())) {
            throw new CustomException(MemberError.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다.");
        }

        // username 중복(선택값이므로 있을 때만 검사)
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && memberRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(MemberError.CONFLICT, "이미 사용중인 아이디입니다.");
        }

        validatePhoneDuplication(request.getPhone());

        switch (selectRole) {
            case TEACHER -> signupTeacher(request);
            case STUDENT -> signupStudent(request);
            case PARENT  -> signupParent(request);
            default      -> throw new CustomException(MemberError.BAD_REQUEST);
        }
    }

    // 전화번호 로그인(토큰 발급/회전)
    @Override
    public ResponseToken login(String phone, String password) {
        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(MemberError.INVALID_PASSWORD);
        }

        String roleClaim = "ROLE_" + member.getRole().name();
        RefreshToken refresh = refreshTokenRepository.findByPhone(phone).orElse(null);

        String accessToken;
        String refreshTokenString;

        if (refresh != null) {
            if (!refresh.isExpired()) {
                accessToken = jwtProvider.generateAccessTokenByPhone(phone, roleClaim, member.getUsername(), member.getName());
                refreshTokenString = refresh.getToken();
            } else {
                ResponseToken pair = jwtProvider.generateTokenByPhone(phone, roleClaim, member.getUsername(), member.getName());
                refresh.rotate(pair.getRefreshToken(), LocalDateTime.now().plusDays(28));
                accessToken = pair.getAccessToken();
                refreshTokenString = pair.getRefreshToken();
            }
        } else {
            ResponseToken pair = jwtProvider.generateTokenByPhone(phone, roleClaim, member.getUsername(), member.getName());
            RefreshToken newRt = RefreshToken.builder()
                    .phone(phone)
                    .token(pair.getRefreshToken())
                    .expiryDate(LocalDateTime.now().plusDays(28))
                    .build();
            refreshTokenRepository.save(newRt);

            accessToken = pair.getAccessToken();
            refreshTokenString = pair.getRefreshToken();
        }

        return new ResponseToken(accessToken, refreshTokenString);
    }

    // username 만들기(선택)
    @Override
    public void createUsername(String username) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getName(); // username 또는 phone(필터 정책)

        // principal로 먼저 username 조회, 실패 시 phone 조회
        Member member = memberRepository.findByUsername(principal)
                .or(() -> memberRepository.findByPhone(principal))
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (username == null || username.isBlank()) {
            throw new CustomException(MemberError.BAD_REQUEST, "username은 비워둘 수 없습니다.");
        }
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(MemberError.CONFLICT, "이미 사용중인 닉네임입니다.");
        }

        member.setUsername(username);
        memberRepository.save(member);
    }

    @Override
    public String findUsername(String phone) {
        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        return member.getUsername();
    }

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

    // 로그아웃: 특정 refresh 토큰만 폐기 (이제 phone 기준)
    @Override
    public void logout(String phone, String refreshToken) {
        // 중복 정리를 피하고 싶으면 controller에서 cleanup 후 raw rt를 넘긴다
        String rt = cleanupToken(refreshToken);
        boolean exists = refreshTokenRepository.existsByPhoneAndToken(phone, rt);
        if (!exists) throw new CustomException(TokenError.UNAUTHORIZED);
        refreshTokenRepository.deleteByPhoneAndToken(phone, rt);
    }

    private String cleanupToken(String token) {
        if (token == null) return null;
        String v = token.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) v = v.substring(7).trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
        return v;
    }

    public ResponseToken reissue(String refreshToken) {
        String rt = cleanupToken(refreshToken);

        if (isLikelyJwe(rt)) {
            rt = jwtProvider.decryptToken(rt);
        }

        jwtProvider.validateToken(rt);

        // subject = phone
        String phone = jwtProvider.getPhone(rt);
        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        RefreshToken saved = refreshTokenRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(TokenError.UNAUTHORIZED));
        if (!saved.getToken().equals(rt) || saved.isExpired()) {
            throw new CustomException(TokenError.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }

        String roleClaim = "ROLE_" + member.getRole().name();
        String newAccessToken = jwtProvider.generateAccessTokenByPhone(phone, roleClaim, member.getUsername(), member.getName());

        return new ResponseToken(newAccessToken, rt);
    }

    private void signupTeacher(SignupRequest request) {
        String academyCode = request.getAcademyCode();
        String userInputCode = request.getVerificationCode();

        if (academyCode == null || academyCode.trim().isEmpty()) throw new CustomException(MemberError.BAD_REQUEST);
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        if (userInputCode == null || userInputCode.trim().isEmpty()) throw new CustomException(MemberError.BAD_REQUEST);
        AcademyVerificationCode savedCode = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!savedCode.getCode().equals(userInputCode)) throw new CustomException(MemberError.BAD_REQUEST);
        if (savedCode.getExpiry().isBefore(LocalDateTime.now())) throw new CustomException(MemberError.TOKEN_EXPIRED);
        academyVerificationCodeRepository.delete(savedCode);

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .academy(academy)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        memberRepository.save(member);
    }

    private void signupStudent(SignupRequest request) {
        String academyCode = request.getAcademyCode();
        if (academyCode == null || academyCode.trim().isEmpty()) throw new CustomException(MemberError.BAD_REQUEST);
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .academy(academy)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        memberRepository.save(member);
    }

    private void signupParent(SignupRequest request) {
        List<String> studentUsernames = request.getStudentUsername();
        if (studentUsernames == null || studentUsernames.isEmpty()) throw new CustomException(MemberError.BAD_REQUEST);

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
                .parentStudents(children)
                .build();

        memberRepository.save(parent);
    }

    private void validatePhoneDuplication(String phone) {
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new CustomException(MemberError.CONFLICT, "이미 사용중인 전화번호입니다.");
        }
    }

    private boolean isLikelyJwe(String t) {
        if (t == null) return false;
        int dots = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dots++;
        return dots == 4;
    }
    // 회원가입 코드(선생님)
//    @Override
//    public void sendSignupVerificationCode(String academyCode, String name) {
//        if (academyCode == null) {
//            throw new CustomException(MemberError.BAD_REQUEST);
//        }
//        Academy academy = academyRepository.findByAcademyCode(academyCode)
//                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
//        String email = academy.getEmail();
//        String academyName = academy.getAcademyName();
//
//        String tempCode = UUID.randomUUID().toString().substring(0, 13);
//        AcademyVerificationCode verificationCode = AcademyVerificationCode.builder()
//                .academyCode(academyCode)
//                .code(tempCode)
//                .expiry(LocalDateTime.now().plusMinutes(10))
//                .build();
//        academyVerificationCodeRepository.save(verificationCode);
//
//        String subject = "회원가입 인증코드 안내";
//        String text = String.format(
//                "%s님이 %s 학원으로 가입하려고 합니다.%n아래 인증코드를 10분 내에 입력해주세요.%n%n인증코드: %s",
//                name, academyName, tempCode
//        );
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject(subject);
//        message.setText(text);
//        javaMailSender.send(message);
//    }

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
    @Override
    public void addStudentsToTeacher(String teacherUsername, List<String> studentUsernames, String password) {
        if (teacherUsername == null || teacherUsername.isBlank() || studentUsernames == null || studentUsernames.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        if (password == null || password.isBlank()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }

        // 1. teacher 객체를 먼저 가져온다!
        Member teacher = memberRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        // 2. 비밀번호 체크
        if (!passwordEncoder.matches(password, teacher.getPassword())) {
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        }

        if (teacher.getRole() != Role.TEACHER) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }

        for (String s : studentUsernames) {
            Member student = memberRepository.findByUsername(s)
                    .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + s));
            teacher.addStudent(student); // 양방향 연결
        }
        memberRepository.save(teacher);
    }

    @Override
    public void removeStudentsFromTeacher(String teacherUsername, List<String> studentUsernames) {
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
    public java.util.List<String> listStudentsOfTeacher(String requesterUsername, String teacherUsername) {
        // teacher 조회(연관관계 포함)
        Member teacher = memberRepository.findWithRelationsByUsername(teacherUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));

        // 요청자 필수
        if (requesterUsername == null || requesterUsername.isBlank()) {
            throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다. 로그인 후 시도하세요.");
        }
        Member requester = memberRepository.findWithRelationsByUsername(requesterUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "요청자 정보를 찾을 수 없습니다."));

        // 교사가 맡고 있는 학생 username 목록
        java.util.Set<String> teacherStudents = teacher.getTeachingStudents().stream()
                .map(Member::getUsername)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 권한 규칙
        // 1) 해당 교사 본인(TEACHER + 동일 username) → 전부 반환
        if (requester.getRole() == Role.TEACHER && requesterUsername.equals(teacherUsername)) {
            return teacherStudents.stream().sorted().toList();
        }

        // 2) 부모 → 자신의 자녀와 교사의 학생 교집합만 반환
        if (requester.getRole() == Role.PARENT) {
            java.util.Set<String> parentChildren = requester.getParentStudents().stream()
                    .map(Member::getUsername)
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            return parentChildren.stream()
                    .filter(teacherStudents::contains)
                    .sorted()
                    .toList();
        }

        // 그 외는 금지
        throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
    }

    @Override
    public java.util.List<String> listTeachersOfStudent(String requesterUsername, String studentUsername) {
        // student 조회(연관관계 포함)
        Member student = memberRepository.findWithRelationsByUsername(studentUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다."));

        if (requesterUsername == null || requesterUsername.isBlank()) {
            throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다. 로그인 후 시도하세요.");
        }
        Member requester = memberRepository.findWithRelationsByUsername(requesterUsername)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "요청자 정보를 찾을 수 없습니다."));

        java.util.Set<String> studentTeachers = student.getTeachers().stream()
                .map(Member::getUsername)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        switch (requester.getRole()) {
            case STUDENT -> {
                // 본인만 허용
                if (requesterUsername.equals(studentUsername)) {
                    return studentTeachers.stream().sorted().toList();
                }
            }
            case PARENT -> {
                // 요청자가 해당 학생의 부모인지 확인
                boolean isParentOf = student.getParents().stream()
                        .map(Member::getUsername)
                        .filter(Objects::nonNull)
                        .anyMatch(u -> u.equals(requesterUsername));
                if (isParentOf) {
                    return studentTeachers.stream().sorted().toList();
                }
            }
            case TEACHER -> {
                // 요청자가 그 학생의 담당 교사라면 허용
                if (studentTeachers.contains(requesterUsername)) {
                    return studentTeachers.stream().sorted().toList();
                }
            }
            default -> {
                // 그 외 역할은 허용하지 않음
            }
        }

        throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
    }

}


