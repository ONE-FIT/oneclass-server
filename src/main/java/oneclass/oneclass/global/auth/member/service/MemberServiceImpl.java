package oneclass.oneclass.global.auth.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.academy.entity.Academy;
import oneclass.oneclass.global.auth.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.global.auth.academy.error.AuthError;
import oneclass.oneclass.global.auth.academy.repository.AcademyRepository;
import oneclass.oneclass.global.auth.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.global.auth.member.dto.ResponseToken;
import oneclass.oneclass.global.auth.member.dto.SignupRequest;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.global.auth.member.entity.RefreshToken;
import oneclass.oneclass.global.auth.member.entity.Role;
import oneclass.oneclass.global.auth.member.entity.VerificationCode;
import oneclass.oneclass.global.auth.member.error.MemberError;
import oneclass.oneclass.global.auth.member.jwt.JwtProvider;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import oneclass.oneclass.global.auth.member.repository.RefreshTokenRepository;
import oneclass.oneclass.global.auth.member.repository.VerificationCodeRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
    private final JavaMailSender javaMailSender;
    private final AcademyVerificationCodeRepository academyVerificationCodeRepository;

    @Override
    public void sendSignupVerificationCode(String academyCode) {
        if (academyCode == null) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));
        String email = academy.getEmail();

        // 인증코드 생성 및 저장
        String tempCode = UUID.randomUUID().toString().substring(0, 13);
        AcademyVerificationCode verificationCode = AcademyVerificationCode.builder()
                .academyCode(academyCode)
                .code(tempCode)
                .expiry(LocalDateTime.now().plusMinutes(10))
                .build();
        academyVerificationCodeRepository.save(verificationCode);

        // 메일 발송
        String subject = "회원가입 인증코드 안내";
        String text = "인증코드: " + tempCode + "\n10분 내에 입력해주세요.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
    }

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        Role selectRole = request.getRole();

        if (selectRole == null) {
            throw new CustomException(MemberError.BAD_REQUEST);
        }

        // 아이디 중복 검사 (공통)
        if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new CustomException(MemberError.CONFLICT);
        }

        switch (selectRole) {
            case TEACHER: {
                String academyCode = request.getAcademyCode();
                String userInputCode = request.getVerificationCode();

                if (academyCode == null || academyCode.trim().isEmpty()) {
                    throw new CustomException(MemberError.BAD_REQUEST);
                }
                Academy academy = academyRepository.findByAcademyCode(academyCode)
                        .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));
                if (userInputCode == null || userInputCode.trim().isEmpty()) {
                    throw new CustomException(MemberError.BAD_REQUEST);
                }

                // 인증코드 검증
                AcademyVerificationCode savedCode = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                        .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));
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
                        .role(selectRole)
                        .academy(academy)
                        .name(request.getName())
                        .phone(request.getPhone())
                        .email(request.getEmail())
                        .build();

                //이메일 전번 중복검사
                if(memberRepository.findByEmailOrPhone(request.getEmail(), request.getPhone()).isPresent()){
                    throw new CustomException(MemberError.CONFLICT,"이미 사용중인 이메일 또는 전화번호입니다.");
                }

                memberRepository.save(member);
                break;
            }
            case STUDENT: {
                String academyCode = request.getAcademyCode();
                if (academyCode == null || academyCode.trim().isEmpty()) {
                    throw new CustomException(MemberError.BAD_REQUEST);
                }
                Academy academy = academyRepository.findByAcademyCode(academyCode)
                        .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));

                Member member = Member.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(selectRole)
                        .academy(academy)
                        .name(request.getName())
                        .phone(request.getPhone())
                        .email(request.getEmail())
                        .build();

                //이메일 전번 중복검사
                if(memberRepository.findByEmailOrPhone(request.getEmail(), request.getPhone()).isPresent()){
                    throw new CustomException(MemberError.CONFLICT,"이미 사용중인 이메일 또는 전화번호입니다.");
                }

                memberRepository.save(member);
                break;
            }
            case PARENT: {
                String studentUsername = request.getStudentId();
                if (studentUsername == null || studentUsername.trim().isEmpty()) {
                    throw new CustomException(MemberError.BAD_REQUEST); // 자녀 정보 필수
                }
                Member student = memberRepository.findByUsername(studentUsername)
                        .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND)); // 자녀 존재 확인

                Member parent = Member.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(selectRole)
                        .name(request.getName())
                        .phone(request.getPhone())
                        .email(request.getEmail())
                        .student(student)
                        .build();
                //이메일 전번 중복검사
                if(memberRepository.findByEmailOrPhone(request.getEmail(), request.getPhone()).isPresent()){
                    throw new CustomException(MemberError.CONFLICT,"이미 사용중인 이메일 또는 전화번호입니다.");
                }

                memberRepository.save(parent);
                break;
            }
            default:
                throw new CustomException(MemberError.BAD_REQUEST);
        }
    }

    @Override
    public ResponseToken login(String username, String password){
        //회원정보 조회 with ID
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        //비번 체크
        if (!passwordEncoder.matches(password, member.getPassword())){
            throw new CustomException(MemberError.INVALID_PASSWORD);
        }
        // 토큰 생성 시
        String roleClaim = "ROLE_" + member.getRole().name();
        ResponseToken tokens = jwtProvider.generateToken(member.getUsername(), roleClaim);

        refreshTokenRepository.findByUsername(username);
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(tokens.getRefreshToken())
                .expiryDate(LocalDateTime.now().plusDays(28))
                .build();
        refreshTokenRepository.save(refreshToken);



        return tokens;

    }

    @Override
    public String findUsername(String emailOrPhone) {
        // 이메일 또는 전화번호로 회원 찾기
        Member member = memberRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        return member.getUsername();
    }

    @Override
    public void sendResetPasswordEmail(String emailOrPhone) {
        Member member = memberRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));
        String username = member.getUsername(); // username을 인증코드 키로 사용
        String tempCode = UUID.randomUUID().toString().substring(0, 6);

        verificationCodeRepository.save(
                VerificationCode.builder()
                        .usernameOrEmail(username)
                        .code(tempCode)
                        .expiry(System.currentTimeMillis() + 5 * 60 * 1000)
                        .build()
        );

        emailService.sendSimpleMail(member.getEmail(), "비밀번호 재설정", "인증코드: " + tempCode);
    }

    @Override
    public void resetPassword(String username, String newPassword, String verificationCode) {
        // 인증코드 조회
        VerificationCode codeEntry = verificationCodeRepository.findById(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        // 인증코드 확인
        if (!codeEntry.getCode().equals(verificationCode)) {
            throw new CustomException(MemberError.INVALID_PASSWORD);//토큰이 일치하지 않아서 바꿔야됨
        }

        // 인증코드 만료 확인
        if (codeEntry.getExpiry() < System.currentTimeMillis()) {
            throw new CustomException(MemberError.TOKEN_EXPIRED);
        }

        // 인증코드 삭제
        verificationCodeRepository.deleteById(username);

        // 회원 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(newPassword));

        memberRepository.save(member);
    }

    //로그아웃시 토큰 폐기
    @Override
    public void logout(String username){
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUsername(username);
        if (tokenOpt.isEmpty()) {
            throw new CustomException(MemberError.CONFLICT);
        }
        refreshTokenRepository.deleteByUsername(username);
    }
}

