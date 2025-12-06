package oneclass.oneclass.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.request.AdminSignupRequest;
import oneclass.oneclass.domain.member.dto.request.LoginRequest;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;
import oneclass.oneclass.domain.member.dto.response.MemberDto;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;
import oneclass.oneclass.domain.member.entity.*;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.member.repository.RefreshTokenRepository;
import oneclass.oneclass.domain.member.repository.VerificationCodeRepository;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.auth.jwt.TokenUtils;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AcademyRepository academyRepository;
    private final AcademyVerificationCodeRepository academyVerificationCodeRepository;
    private final JavaMailSender javaMailSender;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    @Value("${app.admin.email}")
    private String serviceAdminEmail;

    @Value("${app.admin.code-valid-minutes:10}")
    private long codeValidityMinutes;

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

    @Override
    public Optional<Long> findMemberIdByUsername(String username) {
        return memberRepository.findByUsername(username)
                .map(Member::getId);
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
    @Transactional
    public void signupAdmin(AdminSignupRequest request) {
        if (request.password() == null || !request.password().equals(request.checkPassword())) {
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        }
        if (serviceAdminEmail == null || serviceAdminEmail.isBlank()) {
            throw new CustomException(MemberError.BAD_REQUEST, "관리자 이메일이 설정되지 않았습니다.");
        }

        final String adminEmailKey = serviceAdminEmail.trim().toLowerCase();

        // 전송 단계
        if (request.verificationCode() == null || request.verificationCode().isBlank()) {
            final String code = generateNumericCode();
            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime expiry = now.plusMinutes(codeValidityMinutes);

            VerificationCode vc = VerificationCode.builder()
                    .identifier(adminEmailKey)
                    .type(VerificationCode.Type.ADMIN_EMAIL) // 관리자용 타입으로 구분
                    .phone(request.phone())
                    .code(code)
                    .expiry(expiry)
                    .used(false)
                    .build();

            verificationCodeRepository.save(vc);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendAdminVerificationEmail(
                            serviceAdminEmail,
                            "인증코드: " + code + "\n유효시간: " + codeValidityMinutes + "분"
                    );
                }
            });

            return;
        }

        // 검증 단계
        VerificationCode stored = verificationCodeRepository
                .findTopByIdentifierAndTypeAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
                        adminEmailKey, VerificationCode.Type.ADMIN_EMAIL, LocalDateTime.now())
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND_VERIFICATION_CODE));

        if (stored.getExpiry().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteByIdentifierAndType(adminEmailKey, VerificationCode.Type.ADMIN_EMAIL);
            throw new CustomException(MemberError.EXPIRED_VERIFICATION_CODE);
        }
        if (!normalizeCode(stored.getCode()).equals(normalizeCode(request.verificationCode()))) {
            throw new CustomException(MemberError.INVALID_VERIFICATION_CODE);
        }

        // 일회성 사용 처리
        stored.setUsed(true);
        verificationCodeRepository.save(stored);
        // 또는 삭제로 관리:
        // verificationCodeRepository.deleteByIdentifierAndType(adminEmailKey, VerificationCode.Type.ADMIN_EMAIL);

        // 관리자 생성
        if (memberRepository.existsByUsername(request.username())) {
            throw new CustomException(MemberError.CONFLICT, "이미 사용중인 아이디입니다.");
        }

        Member admin = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .name(request.name())
                .email(request.email())
                .role(Role.ADMIN)
                .build();

        memberRepository.save(admin);
    }

    private void sendAdminVerificationEmail(String to, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[서비스 관리자 인증] 관리자 계정 생성 인증코드");
        message.setText(text);
        // from 설정은 application.yml mail.username 이거나 여기서 직접 설정 가능
        javaMailSender.send(message);
    }
    private String generateNumericCode() {
        final int VERIFICATION_CODE_LENGTH = 6;
        StringBuilder sb = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }


    @Override
    @Transactional
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

        // 1) 회원 존재 확인(유효 코드 소모 방지)
        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        // 2) 현재 유효(미사용 + 만료 전) + 비번 재설정 타입의 최신 코드 조회
        var codeEntry = verificationCodeRepository
                .findTopByPhoneAndTypeAndUsedFalseAndExpiryAfterOrderByExpiryDesc(
                        phone, VerificationCode.Type.RESET_PASSWORD, LocalDateTime.now())
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND_VERIFICATION_CODE));


        // 3) 코드 비교
        if (!normalizeCode(codeEntry.getCode()).equals(normalizeCode(verificationCode))) {
            throw new CustomException(MemberError.INVALID_VERIFICATION_CODE);
        }

        // 4) 일회성 처리
        codeEntry.setUsed(true);
        // codeEntry.setUsedAt(LocalDateTime.now()); // 필드가 있다면 사용
        verificationCodeRepository.save(codeEntry);

        // 5) 비밀번호 변경
        member.setPassword(passwordEncoder.encode(newPassword));
        // 영속 상태이므로 @Transactional 커밋 시 반영

        // 6) 커밋 후 SMS 안내 발송(원하면 사용)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // smsResetPasswordCode.send("비밀번호가 성공적으로 재설정되었습니다.", phone);
            }
        });
    }

    private String normalizeCode(String code) {
        if (code == null) return "";
        return code.trim().replaceAll("\\s+", "").toUpperCase();
    }
    @Override
    public String cleanupToken(String token) {
        return TokenUtils.cleanup(token);
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
        for (Member child : children) {
            if (already.contains(child)) continue; // 이미 연결된 경우 무시
            parent.addParentStudent(child);
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
    @Transactional
    public void removeStudentsFromTeacher(String teacherPhone, List<String> studentPhones, Authentication authentication) {
        if (teacherPhone == null || teacherPhone.isBlank() || studentPhones == null || studentPhones.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST, "교사/학생 정보가 필요합니다.");
        }
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new CustomException(MemberError.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        // 교사 조회
        Member teacher = memberRepository.findByPhone(teacherPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));

        // 본인 확인: 인증된 사용자만 자신의 학생 목록을 수정 가능
        // 인증 주체는 일반적으로 username(로그인 ID)입니다.
        if (!teacher.getUsername().equals(authentication.getName())) {
            throw new CustomException(MemberError.FORBIDDEN, "자신의 학생 목록만 수정할 수 있습니다.");
        }

        // 역할 확인
        if (teacher.getRole() != Role.TEACHER) {
            throw new CustomException(MemberError.BAD_REQUEST, "해당 사용자는 선생님이 아닙니다.");
        }

        // 학생 조회 맵 구성
        List<Member> students = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = students.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m, (existing, replacement) -> existing));

        // 제거 처리
        for (String phone : studentPhones) {
            Member student = byPhone.get(phone);
            if (student == null) {
                throw new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + phone);
            }
            teacher.removeStudent(student);
        }

        memberRepository.save(teacher);
    }
}