package oneclass.oneclass.domain.academy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.academy.dto.request.AcademySignupRequest;
import oneclass.oneclass.domain.academy.dto.request.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.academy.dto.response.AcademySignupResponse;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyRefreshToken;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.entity.Role;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRefreshTokenRepository;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AcademyServiceImpl implements AcademyService {

    private final AcademyRepository academyRepository;
    private final JwtProvider jwtProvider;
    private final AcademyRefreshTokenRepository academyRefreshTokenRepository;
    private final AcademyVerificationCodeRepository academyVerificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@[\\w-\\.]+\\.\\w+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    public String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    @Override
    public AcademySignupResponse academySignup(AcademySignupRequest request) {
        validateSignupRequest(request);

        Role role = Role.ACADEMY;
        String randomAcademyCode;

        if (academyRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomException(AcademyError.DUPLICATE_EMAIL);
        }
        if (academyRepository.findByPhone(request.phone()).isPresent()) {
            throw new CustomException(AcademyError.DUPLICATE_PHONE);
        }

        do {
            randomAcademyCode = generateRandomCode(8);
        } while (academyRepository.findByAcademyCode(randomAcademyCode).isPresent());

        Academy academy = Academy.builder()
                .role(role)
                .academyCode(randomAcademyCode)
                .academyName(request.academyName())
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .build();

        academyRepository.save(academy);

        return new AcademySignupResponse(
                randomAcademyCode,
                request.academyName(),
                request.email(),
                request.phone()
        );
    }

    private void validateSignupRequest(AcademySignupRequest request) {
        if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
            throw new CustomException(AcademyError.INVALID_EMAIL_FORMAT, "이메일 형식이 유효하지 않습니다.");
        }
        if (!PHONE_PATTERN.matcher(request.phone()).matches()) {
            throw new CustomException(AcademyError.INVALID_PHONE_FORMAT, "전화번호는 10~11자리 숫자여야 합니다.");
        }
        if (request.password().length() < 8) {
            throw new CustomException(AcademyError.PASSWORD_TOO_SHORT);
        }
        if (request.academyName() == null || request.academyName().isBlank()) {
            throw new CustomException(AcademyError.INVALID_ACADEMY_NAME, "학원 이름이 비어있거나 유효하지 않습니다.");
        }
    }

    @Override
    public ResponseToken login(String academyCode, String academyName, String password) {
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND, "학원을 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, academy.getPassword())) {
            throw new CustomException(AcademyError.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        if (!academy.getAcademyName().equalsIgnoreCase(academyName)) {
            throw new CustomException(AcademyError.INVALID_ACADEMY_NAME, "학원 이름이 일치하지 않습니다.");
        }

        String roleClaim = "ROLE_" + academy.getRole().name();
        AcademyRefreshToken refreshToken = getOrCreateRefreshToken(academyCode, roleClaim);

        return new ResponseToken(
                jwtProvider.generateAccessToken(academyCode, roleClaim),
                refreshToken.getToken()
        );
    }

    private AcademyRefreshToken getOrCreateRefreshToken(String academyCode, String roleClaim) {
        return academyRefreshTokenRepository.findByAcademyCode(academyCode)
                .map(saved -> {
                    if (jwtProvider.isTokenInvalid(saved.getToken())) {
                        ResponseToken pair = jwtProvider.generateToken(academyCode, roleClaim);
                        saved.rotate(pair.refreshToken(), LocalDateTime.now().plusDays(28));
                    }
                    return saved;
                })
                .orElseGet(() -> {
                    ResponseToken pair = jwtProvider.generateToken(academyCode, roleClaim);
                    AcademyRefreshToken tokenToSave = AcademyRefreshToken.builder()
                            .academyCode(academyCode)
                            .token(pair.refreshToken())
                            .expiryDate(LocalDateTime.now().plusDays(28))
                            .build();
                    return academyRefreshTokenRepository.save(tokenToSave);
                });
    }

    @Override
    public void sendResetPasswordEmail(String code, String name) {
        Academy academy = academyRepository.findByAcademyCodeAndAcademyName(code, name)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND, "학원을 찾을 수 없습니다."));

        String tempCode = UUID.randomUUID().toString().substring(0, 13);

        AcademyVerificationCode verificationCode = AcademyVerificationCode.builder()
                .academyCode(academy.getAcademyCode())
                .code(tempCode)
                .expiry(LocalDateTime.now().plusMinutes(10))
                .build();

        academyVerificationCodeRepository.save(verificationCode);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(academy.getEmail());
            message.setSubject("비밀번호 재설정 인증코드");
            message.setText("인증코드: " + tempCode + "\n10분 내에 입력해주세요.");
            javaMailSender.send(message);
        } catch (org.springframework.mail.MailException e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new CustomException(AcademyError.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void resetPassword(ResetAcademyPasswordRequest request) {
        AcademyVerificationCode codeEntity = academyVerificationCodeRepository.findByAcademyCode(request.academyCode())
                .orElseThrow(() -> new CustomException(AcademyError.VERIFICATION_CODE_NOT_FOUND));

        verifyCodeExpirationAndMatch(codeEntity, request.verificationCode());

        Academy academy = academyRepository.findByAcademyCode(request.academyCode())
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!academy.getAcademyName().equalsIgnoreCase(request.academyName())) {
            throw new CustomException(AcademyError.INVALID_ACADEMY_NAME, "학원 이름이 일치하지 않습니다.");
        }

        academy.setPassword(passwordEncoder.encode(request.newPassword()));
        academyRepository.save(academy);
    }

    private void verifyCodeExpirationAndMatch(AcademyVerificationCode entity, String verificationCode) {
        if (!entity.getCode().equals(verificationCode)) {
            throw new CustomException(AcademyError.INVALID_VERIFICATION_CODE);
        }
        if (entity.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(AcademyError.EXPIRED_VERIFICATION_CODE);
        }
    }

    @Override
    public void logout(String academyCode, String refreshToken) {
        boolean exists = academyRefreshTokenRepository.existsByAcademyCodeAndToken(academyCode, refreshToken);
        if (!exists) {
            throw new CustomException(AcademyError.ALREADY_LOGGED_OUT, "이미 로그아웃된 토큰입니다.");
        }
        academyRefreshTokenRepository.deleteByAcademyCodeAndToken(academyCode, refreshToken);
    }
}