package oneclass.oneclass.domain.member.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.request.LoginRequest;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;
import oneclass.oneclass.domain.member.dto.response.MemberDto;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.entity.RefreshToken;
import oneclass.oneclass.domain.member.entity.Role;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AcademyRepository academyRepository;
    private final AcademyVerificationCodeRepository academyVerificationCodeRepository;
    private final JavaMailSender javaMailSender;

    // 1) 회원가입: 2번째(Username 기반 DTO) 형태를 유지하되, 예외/검증은 기존 스타일
    @Override
    public void signup(SignupRequest request) {
        Role selectRole = request.role();
        if (selectRole == null) throw new CustomException(MemberError.BAD_REQUEST);

        validatePhoneDuplication(request.phone());

        // 비밀번호 확인
        if (request.password() == null || request.checkPassword() == null
                || !request.password().equals(request.checkPassword())) {
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        }

        // username 중복
        if (memberRepository.findByUsername(request.username()).isPresent()) {
            throw new CustomException(MemberError.DUPLICATE_USERNAME);
        }

        // 이메일/전화 중복(필요 시 구현)
        validatePhoneDuplication(request.phone());

        // 역할별 처리
        switch (selectRole) {
            case TEACHER -> signupTeacher(request);
            case STUDENT -> signupStudent(request);
            case PARENT  -> signupParent(request);
            default      -> throw new CustomException(MemberError.BAD_REQUEST);
        }
    }

    @Override
    public ResponseToken login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(MemberError.UNAUTHORIZED);
        }

        String roleClaim = "ROLE_" + member.getRole().name();
        RefreshToken existing = refreshTokenRepository.findByUsername(request.username()).orElse(null);

        ResponseToken pair;

        if (existing != null && !existing.isExpired()) {
            // 기존 refresh 유지, access만 새로
            String access = jwtProvider.generateAccessToken(request.username(), roleClaim);
            return new ResponseToken(access, existing.getToken());
        }

        // 새 쌍(만료되었거나 최초)
        pair = jwtProvider.generateToken(request.username(), roleClaim);
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(28);

        if (existing != null) {
            existing.rotate(pair.refreshToken(), newExpiry);
        } else {
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .username(request.username())
                            .token(pair.refreshToken())
                            .expiryDate(newExpiry)
                            .build()
            );
        }
        return new ResponseToken(pair.accessToken(), pair.refreshToken());
    }

    // 로그아웃: username + 특정 refreshToken 폐기 (1번 스타일 유지)
    @Override
    public void logout(String username, String refreshToken) {
        boolean exists = refreshTokenRepository.existsByUsernameAndToken(username, refreshToken);
        if (!exists) throw new CustomException(TokenError.NOT_FOUND);
        refreshTokenRepository.deleteByUsernameAndToken(username, refreshToken);
    }

    // 재발급: 1번 스타일 유지, subject = username
    @Override
    public ResponseToken reissue(String refreshToken) {
        String rt = cleanupToken(refreshToken); // 기존 유틸 사용 가정
        jwtProvider.validateToken(rt);

        // subject = username
        String username = jwtProvider.getUsername(rt);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        RefreshToken saved = refreshTokenRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(TokenError.NOT_FOUND));
        if (!saved.getToken().equals(rt) || saved.isExpired()) {
            throw new CustomException(TokenError.TOKEN_EXPIRED, "리프레시 토큰이 유효하지 않습니다.");
        }

        String roleClaim = "ROLE_" + member.getRole().name();
        String newAccessToken = jwtProvider.generateAccessToken(username, roleClaim);

        return new ResponseToken(newAccessToken, rt);
    }

    private void signupTeacher(SignupRequest request) {
        String academyCode = request.academyCode();
        String userInputCode = request.verificationCode();

        if (academyCode == null || academyCode.trim().isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        if (userInputCode == null || userInputCode.trim().isEmpty()) {
            throw new CustomException(MemberError.VERIFICATION_CODE_REQUIRED);
        }
        AcademyVerificationCode savedCode = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.VERIFICATION_CODE_NOT_FOUND));
        if (!savedCode.getCode().equals(userInputCode)) {
            throw new CustomException(MemberError.INVALID_VERIFICATION_CODE);
        }
        if (savedCode.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(MemberError.EXPIRED_VERIFICATION_CODE);
        }
        academyVerificationCodeRepository.delete(savedCode);

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.TEACHER)
                .academy(academy)
                .name(request.name())
                .phone(request.phone())
                .build();

        memberRepository.save(member);
    }

    private void signupStudent(SignupRequest request) {
        String academyCode = request.academyCode();
        if (academyCode == null || academyCode.trim().isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .academy(academy)
                .name(request.name())
                .phone(request.phone())
                .build();

        memberRepository.save(member);
    }

    private void signupParent(SignupRequest request) {
        // 1) 단일 자녀 username 검증
        String childUsername = request.childUsername();
        if (childUsername == null || childUsername.isBlank()) {
            throw new CustomException(MemberError.BAD_REQUEST, "childUsername(자녀 아이디)가 필요합니다.");
        }
        if (childUsername.equals(request.username())) {
            throw new CustomException(MemberError.BAD_REQUEST, "자기 자신을 자녀로 등록할 수 없습니다.");
        }

        // 2) 자녀 조회 + 역할 검증
        Member child = memberRepository.findByUsername(childUsername.trim())
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "자녀 계정을 찾을 수 없습니다: " + childUsername));

        if (child.getRole() != Role.STUDENT) {
            throw new CustomException(MemberError.BAD_REQUEST, "자녀 username은 STUDENT 계정이어야 합니다.");
        }

        // 3) 부모 엔티티 생성
        Member parent = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PARENT) // 안전하게 강제
                .name(request.name())
                .phone(request.phone())
                .build();

        // 4) 관계 연결 (양방향 메서드가 parent.addParentStudent(child)로 구현돼 있다고 가정)
        parent.addParentStudent(child);

        // 5) 저장 (동시성/유니크 제약 대응)
        try {
            memberRepository.save(parent);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new CustomException(MemberError.CONFLICT);
        }
    }

    @Override
    public void resetPassword(String phone, String newPassword, String checkPassword, String verificationCode) {
        if (newPassword == null || !newPassword.equals(checkPassword)) {
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        }
        if (phone == null || phone.isBlank()) {
            throw new CustomException(MemberError.PHONE_REQUIRED);
        }
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new CustomException(MemberError.VERIFICATION_CODE_REQUIRED);
        }

        String provided = normalizeCode(verificationCode);

        var codeEntry = verificationCodeRepository.findById(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND_VERIFICATION_CODE));

        String saved = normalizeCode(codeEntry.getCode());
        if (!saved.equals(provided)) {
            throw new CustomException(MemberError.INVALID_VERIFICATION_CODE);
        }
        if (codeEntry.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(MemberError.EXPIRED_VERIFICATION_CODE);
        }

        verificationCodeRepository.deleteById(phone);

        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    private String normalizeCode(String code) {
        if (code == null) return "";
        return code.trim().replaceAll("\\s+", "").toUpperCase();
    }
    @Override
    public String cleanupToken(String token) {
        if (token == null) return null;
        String v = token.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) v = v.substring(7).trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
        return v;
    }

    private void validatePhoneDuplication(String phone) {
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new CustomException(MemberError.DUPLICATE_PHONE);
        }
    }


    @Override
    public void sendSignupVerificationCode(String academyCode, String name) {
        if (academyCode == null) {
            throw new CustomException(AcademyError.INVALID_ACADEMY_CODE);
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

    @Transactional
    @Override
    public void addStudentsToParent(String parentUsername, String password, List<String> studentUsernames) {
        // 기본 검증
        if (parentUsername == null || parentUsername.isBlank()) {
            throw new CustomException(MemberError.USERNAME_REQUIRED);
        }
        if (studentUsernames == null || studentUsernames.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "자녀 username 목록이 비어있습니다.");
        }

        // 입력 정규화(공백 제거) + 중복 제거
        LinkedHashSet<String> requested = studentUsernames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (requested.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "유효한 자녀 username이 없습니다.");
        }

        // 부모 조회 + 본인 확인(비밀번호)
        Member parent = memberRepository.findByUsername(parentUsername.trim())
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "부모 계정을 찾을 수 없습니다: " + parentUsername));

        if (!passwordEncoder.matches(password, parent.getPassword())) {
            throw new CustomException(MemberError.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }
        // 부모 역할 검증(선택: 강제하고 싶다면)
        if (parent.getRole() != Role.PARENT) {
            throw new CustomException(MemberError.BAD_REQUEST, "해당 계정은 PARENT가 아닙니다: " + parent.getUsername());
        }

        // self-assign 방지
        if (requested.contains(parent.getUsername())) {
            throw new CustomException(MemberError.BAD_REQUEST, "자기 자신을 자녀로 추가할 수 없습니다.");
        }

        // 자녀 일괄 조회 (N+1 제거)
        List<Member> children = memberRepository.findAllByUsernameIn(requested);
        Set<String> found = children.stream().map(Member::getUsername).collect(Collectors.toSet());

        //누락된 username들 수집
        List<String> missing = requested.stream().filter(u -> !found.contains(u)).toList();
        if (!missing.isEmpty()) {
            throw new CustomException(MemberError.NOT_FOUND, "다음 자녀 계정을 찾을 수 없습니다: " + String.join(", ", missing));
        }

        // 역할 검증(학생인지)
        List<String> notStudents = children.stream()
                .filter(c -> c.getRole() != Role.STUDENT)
                .map(Member::getUsername)
                .toList();
        if (!notStudents.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "학생 계정이 아닌 username 포함: " + String.join(", ", notStudents));
        }

        // 이미 연결된 자녀는 건너뛰어 멱등성 보장
        //    parent.getParentStudents()가 Set이라면 contains가 잘 동작하도록 equals/hashCode 구현 확인(보통 id 기반)
        Set<Member> already = parent.getParentStudents() == null ? Set.of() : parent.getParentStudents();
        int added = 0;
        for (Member child : children) {
            if (already.contains(child)) continue; // 이미 연결된 경우 무시
            parent.addParentStudent(child);
            added++;
        }


        // 저장 + 무결성 예외 매핑
        try {
            memberRepository.save(parent); // owning side가 parent라면 join 테이블 갱신됨
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new CustomException(MemberError.CONFLICT, "관계 저장 중 충돌이 발생했습니다.");
        }
    }

    @Override
    public TeacherStudentsResponse addStudentsToTeacher(String teacherPhone, List<String> studentPhones, String password) {
        if (teacherPhone == null || teacherPhone.isBlank() || studentPhones == null || studentPhones.isEmpty())
            throw new CustomException(MemberError.BAD_REQUEST);
        if (password == null || password.isBlank())
            throw new CustomException(MemberError.PASSWORD_REQUEST);

        Member teacher = memberRepository.findByPhone(teacherPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(password, teacher.getPassword()))
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        if (teacher.getRole() != Role.TEACHER)
            throw new CustomException(MemberError.BAD_REQUEST);

        List<Member> students = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = students.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m,
                        (existing, replacement) -> existing));

        for (String phone : studentPhones) {
            Member student = byPhone.get(phone);
            if (student == null)
                throw new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + phone);
            if (student.getRole() != Role.STUDENT)
                throw new CustomException(MemberError.BAD_REQUEST, "학생이 아닌 계정입니다: " + phone);
            teacher.addStudent(student);
        }

        memberRepository.save(teacher);

        MemberDto teacherDto = new MemberDto(
                teacher.getId(),
                teacher.getUsername(),
                teacher.getName(),
                teacher.getPhone(),
                teacher.getRole()
        );

        List<MemberDto> studentsDto = teacher.getTeachingStudents().stream()
                .map(s -> new MemberDto(s.getId(), s.getUsername(), s.getName(), s.getPhone(), s.getRole()))
                .sorted(Comparator.comparing(MemberDto::name, Comparator.nullsLast(Comparator.naturalOrder()))).toList();

        return new TeacherStudentsResponse(teacherDto, studentsDto);
    }

    @Override
    public void removeStudentsFromTeacher(String teacherPhone, List<String> studentPhones) {
        if (teacherPhone == null || teacherPhone.isBlank() || studentPhones == null || studentPhones.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "교사/학생 정보가 필요합니다.");
        }

        Member teacher = memberRepository.findByPhone(teacherPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new CustomException(MemberError.BAD_REQUEST, "해당 사용자는 선생님이 아닙니다.");
        }

        List<Member> students = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = students.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m,
                        (existing, replacement) -> existing));

        for (String phone : studentPhones) {
            Member student = byPhone.get(phone);
            if (student == null) throw new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + phone);
            teacher.removeStudent(student);
        }
        memberRepository.save(teacher);
    }
}