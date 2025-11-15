package oneclass.oneclass.domain.member.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
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
    public ResponseToken login(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(MemberError.UNAUTHORIZED);
        }

        String roleClaim = "ROLE_" + member.getRole().name();
        RefreshToken existing = refreshTokenRepository.findByUsername(username).orElse(null);

        ResponseToken pair;

        if (existing != null && !existing.isExpired()) {
            // 기존 refresh 유지, access만 새로
            String access = jwtProvider.generateAccessToken(username, roleClaim);
            return new ResponseToken(access, existing.getToken());
        }

        // 새 쌍(만료되었거나 최초)
        pair = jwtProvider.generateToken(username, roleClaim);
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(28);

        if (existing != null) {
            existing.rotate(pair.refreshToken(), newExpiry);
        } else {
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .username(username)
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
                .role(request.role())
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
                .role(request.role())
                .academy(academy)
                .name(request.name())
                .phone(request.phone())
                .build();

        memberRepository.save(member);
    }

    private void signupParent(SignupRequest request) {
        List<String> studentUsernames = request.studentId();
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
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .name(request.name())
                .phone(request.phone())
                .build();

        for (Member child : children) {
            parent.addParentStudent(child);
        }

        memberRepository.save(parent);
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
    public void deleteUser(String phone) {
        Member member = memberRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        memberRepository.delete(member);
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

    @Override
    public void addStudentsToParent(String parentPhone, String password, List<String> studentPhones) {
        if (studentPhones == null || studentPhones.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }

        Member parent = memberRepository.findByPhone(parentPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(password, parent.getPassword())) {
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        }

        List<Member> children = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = children.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m,
                        (existing, replacement) -> existing));

        for (String phone : studentPhones) {
            Member child = byPhone.get(phone);
            if (child == null) throw new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + phone);
            if (child.getRole() != Role.STUDENT) throw new CustomException(MemberError.BAD_REQUEST);
            parent.addParentStudent(child);
        }
        memberRepository.save(parent);
    }

    @Override
    public void deleteParent(Long parentId) {
        Member parent = memberRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        parent.getParentStudents().clear();
        memberRepository.save(parent);
        memberRepository.delete(parent);
    }

    @Override
    public TeacherStudentsResponse addStudentsToTeacher(String teacherPhone, List<String> studentPhones, String password) {
        if (teacherPhone == null || teacherPhone.isBlank() || studentPhones == null || studentPhones.isEmpty())
            throw new CustomException(MemberError.BAD_REQUEST);
        if (password == null || password.isBlank())
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);

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

    @Override
    public List<String> listStudentsOfTeacher(String requesterPhone, String teacherPhone) {
        Member teacher = memberRepository.findWithTeachingStudentsByPhone(teacherPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "선생님을 찾을 수 없습니다."));

        if (requesterPhone == null || requesterPhone.isBlank()) {
            throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다. 로그인 후 시도하세요.");
        }
        Member requester = memberRepository.findByPhone(requesterPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "요청자 정보를 찾을 수 없습니다."));

        var teacherStudents = teacher.getTeachingStudents().stream()
                .map(Member::getPhone)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (requester.getRole() == Role.TEACHER && requesterPhone.equals(teacherPhone)) {
            return teacherStudents.stream().sorted().toList();
        }
        if (requester.getRole() == Role.PARENT) {
            var parentChildren = requester.getParentStudents().stream()
                    .map(Member::getPhone)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return parentChildren.stream()
                    .filter(teacherStudents::contains)
                    .sorted()
                    .toList();
        }
        throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
    }

    @Override
    public List<String> listTeachersOfStudent(String requesterPhone, String studentPhone) {
        Member student = memberRepository.findStudentWithTeachersAndParentsByPhoneFetchJoin(studentPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다."));

        if (requesterPhone == null || requesterPhone.isBlank()) {
            throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다. 로그인 후 시도하세요.");
        }

        Member requester = memberRepository.findByPhone(requesterPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "요청자 정보를 찾을 수 없습니다."));

        Set<String> studentTeachersPhones = student.getTeachers().stream()
                .map(Member::getPhone)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        switch (requester.getRole()) {
            case STUDENT -> {
                if (!requesterPhone.equals(studentPhone)) {
                    throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
                }
                return studentTeachersPhones.stream().sorted().toList();
            }
            case PARENT -> {
                boolean isParentOf = student.getParents().stream()
                        .map(Member::getPhone)
                        .filter(Objects::nonNull)
                        .anyMatch(p -> p.equals(requesterPhone));
                if (!isParentOf) {
                    throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
                }
                return studentTeachersPhones.stream().sorted().toList();
            }
            case TEACHER -> {
                if (!studentTeachersPhones.contains(requesterPhone)) {
                    throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
                }
                return studentTeachersPhones.stream().sorted().toList();
            }
            default -> throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
        }
    }
}