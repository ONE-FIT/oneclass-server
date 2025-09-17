package oneclass.oneclass.global.auth.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.academy.dto.AcademyLoginRequest;
import oneclass.oneclass.global.auth.academy.entity.Academy;
import oneclass.oneclass.global.auth.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.global.auth.academy.repository.AcademyRepository;
import oneclass.oneclass.global.auth.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.global.auth.member.dto.ResponseToken;
import oneclass.oneclass.global.auth.member.dto.SignupRequest;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.global.auth.member.entity.RefreshToken;
import oneclass.oneclass.global.auth.member.entity.Role;
import oneclass.oneclass.global.auth.member.entity.VerificationCode;
import oneclass.oneclass.global.auth.member.jwt.JwtProvider;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import oneclass.oneclass.global.auth.member.repository.RefreshTokenRepository;
import oneclass.oneclass.global.auth.member.repository.VerificationCodeRepository;
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
            throw new IllegalArgumentException("학원 코드를 입력해주세요.");
        }
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new IllegalArgumentException("학원 정보를 찾을 수 없습니다."));
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
    public void signup(SignupRequest request) {
        Role selectRole = request.getRole();
        if (selectRole == null) {
            throw new IllegalArgumentException("멤버 유형을 선택해주세요.");
        }

        // 선생님 회원가입 로직
        if (selectRole == Role.TEACHER) {
            String academyCode = request.getAcademyCode();
            String userInputCode = request.getVerificationCode();
            if (academyCode == null) {
                throw new IllegalArgumentException("학원 코드를 입력해주세요.");
            }
            if (userInputCode == null) {
                throw new IllegalArgumentException("인증코드를 입력해주세요.");
            }

            // 인증코드 검증
            AcademyVerificationCode savedCode = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                    .orElseThrow(() -> new IllegalArgumentException("인증코드가 존재하지 않습니다."));
            if (!savedCode.getCode().equals(userInputCode)) {
                throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
            }
            if (savedCode.getExpiry().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("인증코드가 만료되었습니다.");
            }
            academyVerificationCodeRepository.delete(savedCode);

            if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
            }

            Member member = Member.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(selectRole)
                    .academyCode(academyCode)
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .build();
            memberRepository.save(member);
        }
        // 그 외 회원가입 (학생/학부모 등)
        else if (!(selectRole == Role.STUDENT || selectRole == Role.PARENT)) {
            throw new IllegalArgumentException("허용되지 않은 유형입니다.");
        } else {
            if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
            }
            Member member = new Member();
            member.setRole(selectRole);
            member.setUsername(request.getUsername());
            member.setPassword(passwordEncoder.encode(request.getPassword()));
            member.setEmail(request.getEmail());
            member.setPhone(request.getPhone());
            memberRepository.save(member);
        }
    }

    @Override
    public ResponseToken login(String username, String password){
        //회원정보 조회 with ID
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        //비번 체크
        if (!passwordEncoder.matches(password, member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return member.getUsername();
    }

    @Override
    public void sendResetPasswordEmail(String emailOrPhone) {
        Member member = memberRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
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
                .orElseThrow(() -> new IllegalArgumentException("인증코드가 없습니다."));

        // 인증코드 확인
        if (!codeEntry.getCode().equals(verificationCode)) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        // 인증코드 만료 확인
        if (codeEntry.getExpiry() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다.");
        }

        // 인증코드 삭제
        verificationCodeRepository.deleteById(username);

        // 회원 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(newPassword));

        memberRepository.save(member);
    }

    //로그아웃시 토큰 폐기
    @Override
    public void logout(String username){
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUsername(username);
        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("이미 로그아웃된 사용자입니다.");
        }
        refreshTokenRepository.deleteByUsername(username);
    }
}

