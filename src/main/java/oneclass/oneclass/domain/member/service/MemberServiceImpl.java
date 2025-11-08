package oneclass.oneclass.domain.member.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.response.MemberDto;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Override
    public void signup(SignupRequest request) {
        Role selectRole = request.getRole();
        if (selectRole == null) throw new CustomException(MemberError.BAD_REQUEST);

        if (request.getPassword() == null || request.getCheckPassword() == null
                || !request.getPassword().equals(request.getCheckPassword())) {
            throw new CustomException(MemberError.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다.");
        }

        // username 중복(선택값이므로 있을 때만 검사)
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && memberRepository.existsByPhone(request.getPhone())) {
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
        // 컨트롤러에서 이미 토큰을 cleanupToken으로 정리해서 넘김
        boolean exists = refreshTokenRepository.existsByPhoneAndToken(phone, refreshToken);
        if (!exists) throw new CustomException(TokenError.UNAUTHORIZED);
        refreshTokenRepository.deleteByPhoneAndToken(phone, refreshToken);
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
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .academy(academy)
                .name(request.getName())
                .build();

        memberRepository.save(member);
    }

    private void signupStudent(SignupRequest request) {
        String academyCode = request.getAcademyCode();
        if (academyCode == null || academyCode.trim().isEmpty()) throw new CustomException(MemberError.BAD_REQUEST);
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        Member member = Member.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .academy(academy)
                .name(request.getName())
                .build();

        memberRepository.save(member);
    }


    private void signupParent(SignupRequest request) {
        // 자녀 phone 단건 조회 (단일 연결)
        String studentPhone = request.getStudentPhone();
        if (studentPhone == null || studentPhone.isBlank()) {
            throw new CustomException(MemberError.BAD_REQUEST, "자녀 전화번호가 필요합니다.");
        }

        // 부모 생성 (연관관계는 나중에 연결)
        Member parent = Member.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .name(request.getName())
                .build();

        // 자녀 조회 (phone 기준, 단건)
        Member child = memberRepository.findByPhone(studentPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + studentPhone));

        if (child.getRole() != Role.STUDENT) {
            throw new CustomException(MemberError.BAD_REQUEST, "연결하려는 계정이 학생이 아닙니다: " + studentPhone);
        }

        // 양방향 연결
        parent.addParentStudent(child);

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
    //회원가입 코드(선생님)
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
    public void addStudentsToParent(String parentPhone, String password, List<String> studentPhones) {
        if (studentPhones == null || studentPhones.isEmpty()) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }

        // 부모 조회 (phone 기준)
        Member parent = memberRepository.findByPhone(parentPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, parent.getPassword())) {
            throw new CustomException(MemberError.INVALID_PASSWORD);
        }

        // 학생 일괄 조회 (phone 기준)
        List<Member> children = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = children.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m));

        for (String phone : studentPhones) {
            Member child = byPhone.get(phone);
            if (child == null) throw new CustomException(MemberError.NOT_FOUND);
            if (child.getRole() != Role.STUDENT) throw new CustomException(MemberError.BAD_REQUEST);
            parent.addParentStudent(child);
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
    public TeacherStudentsResponse addStudentsToTeacher(String teacherPhone, List<String> studentPhones, String password) {
        if (teacherPhone == null || teacherPhone.isBlank() || studentPhones == null || studentPhones.isEmpty())
            throw new CustomException(MemberError.BAD_REQUEST);
        if (password == null || password.isBlank())
            throw new CustomException(MemberError.BAD_REQUEST);

        Member teacher = memberRepository.findByPhone(teacherPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        if (!passwordEncoder.matches(password, teacher.getPassword()))
            throw new CustomException(MemberError.PASSWORD_CONFIRM_MISMATCH);
        if (teacher.getRole() != Role.TEACHER)
            throw new CustomException(MemberError.BAD_REQUEST);

        // N+1 방지: 일괄 조회
        List<Member> students = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = students.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m));

        for (String phone : studentPhones) {
            Member student = byPhone.get(phone);
            if (student == null)
                throw new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + phone);
            if (student.getRole() != Role.STUDENT)
                throw new CustomException(MemberError.BAD_REQUEST, "학생이 아닌 계정입니다: " + phone);
            teacher.addStudent(student);
        }

        // teacher는 현재 영속성 컨텍스트에 있으므로 바로 save 또는 flush 가능
        memberRepository.save(teacher);

        // 응답용 DTO 생성 (교사 + 현재 교사에게 연결된 모든 학생)
        MemberDto teacherDto = new MemberDto(
                teacher.getId(),
                teacher.getUsername(),
                teacher.getName(),
                teacher.getPhone(),
                teacher.getRole()
        );

        List<MemberDto> studentsDto = teacher.getTeachingStudents().stream()
                .map(s -> new MemberDto(s.getId(), s.getUsername(), s.getName(), s.getPhone(), s.getRole()))
                .sorted((a,b) -> {
                    if (a.getName() == null) return 1;
                    if (b.getName() == null) return -1;
                    return a.getName().compareTo(b.getName());
                })
                .collect(Collectors.toList());

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

        // 학생 일괄 조회
        List<Member> students = memberRepository.findAllByPhoneIn(studentPhones);
        Map<String, Member> byPhone = students.stream()
                .collect(java.util.stream.Collectors.toMap(Member::getPhone, m -> m));

        for (String phone : studentPhones) {
            Member student = byPhone.get(phone);
            if (student == null) throw new CustomException(MemberError.NOT_FOUND, "학생을 찾을 수 없습니다: " + phone);
            teacher.removeStudent(student);
        }
        memberRepository.save(teacher);
    }

    @Override
    public List<String> listStudentsOfTeacher(String requesterPhone, String teacherPhone) {
        // teachingStudents까지 한 번에 로드
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

        // teachers 집합
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
                // 부모-자녀 관계 확인 (이미 parents fetch됨)
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
                // 요청자가 해당 학생의 선생님인지
                if (!studentTeachersPhones.contains(requesterPhone)) {
                    throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
                }
                return studentTeachersPhones.stream().sorted().toList();
            }
            default -> throw new CustomException(MemberError.FORBIDDEN, "조회 권한이 없습니다.");
        }
    }



}