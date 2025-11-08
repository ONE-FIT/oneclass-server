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
import java.util.Optional;
import java.util.UUID;

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

    public String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    @Override
    public AcademySignupResponse academySignup(AcademySignupRequest request) {
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

        // 비밀번호 일치 검증은 DTO(@PasswordMatches)에서 수행

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

    @Override
    public ResponseToken login(String academyCode, String academyName, String password) {
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!passwordEncoder.matches(password, academy.getPassword())) {
            throw new CustomException(AcademyError.UNAUTHORIZED);
        }

        String roleClaim = "ROLE_" + academy.getRole().name();

        Optional<AcademyRefreshToken> refreshOpt =
                academyRefreshTokenRepository.findByAcademyCode(academyCode);

        if (refreshOpt.isPresent()) {
            AcademyRefreshToken saved = refreshOpt.get();

            boolean needRotate = false;
            try {
                jwtProvider.validateToken(saved.getToken());
            } catch (CustomException ex) {
                needRotate = true;
            }

            if (!needRotate && !saved.isExpired()) {
                String accessToken = jwtProvider.generateAccessToken(academyCode, roleClaim);
                return new ResponseToken(accessToken, saved.getToken());
            }

            ResponseToken pair = jwtProvider.generateToken(academyCode, roleClaim);
            saved.rotate(pair.refreshToken(), LocalDateTime.now().plusDays(28));
            return new ResponseToken(pair.accessToken(), pair.refreshToken());
        }

        ResponseToken pair = jwtProvider.generateToken(academyCode, roleClaim);
        AcademyRefreshToken tokenToSave = AcademyRefreshToken.builder()
                .academyCode(academyCode)
                .token(pair.refreshToken())
                .expiryDate(LocalDateTime.now().plusDays(28))
                .build();
        academyRefreshTokenRepository.save(tokenToSave);

        return new ResponseToken(pair.accessToken(), pair.refreshToken());
    }

    @Override
    public void sendResetPasswordEmail(String code, String name){
        Academy academy = academyRepository.findByAcademyCodeAndAcademyName(code , name)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        String academyCode = academy.getAcademyCode();
        String tempCode = UUID.randomUUID().toString().substring(0, 13);

        AcademyVerificationCode verificationCode = AcademyVerificationCode.builder()
                .academyCode(academyCode)
                .code(tempCode)
                .expiry(LocalDateTime.now().plusMinutes(10))
                .build();

        academyVerificationCodeRepository.save(verificationCode);

        String to = academy.getEmail();
        String subject = "비밀번호 재설정 인증코드";
        String text = "인증코드: " + tempCode + "\n10분 내에 입력해주세요.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
    }

    @Override
    public void resetPassword(ResetAcademyPasswordRequest request){
        AcademyVerificationCode codeEntity = academyVerificationCodeRepository.findByAcademyCode(request.academyCode())
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        if (!codeEntity.getCode().equals(request.verificationCode())) {
            throw new CustomException(AcademyError.INVALID_VERIFICATION_CODE);
        }
        if (codeEntity.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(AcademyError.EXPIRED_VERIFICATION_CODE);
        }

        Academy academy = academyRepository.findByAcademyCode(request.academyCode())
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!academy.getAcademyName().equals(request.academyName())) {
            throw new CustomException(AcademyError.NOT_FOUND);
        }

        // 비밀번호 일치 검증은 DTO(@PasswordMatches)에서 수행
        academy.setPassword(passwordEncoder.encode(request.newPassword()));
        academyRepository.save(academy);
    }

    // 특정 Refresh 토큰만 폐기(다중 세션 지원)
    @Override
    public void logout(String academyCode, String refreshToken) {
        boolean exists = academyRefreshTokenRepository.existsByAcademyCodeAndToken(academyCode, refreshToken);
        if (!exists) {
            throw new CustomException(AcademyError.NOT_FOUND);
        }
        academyRefreshTokenRepository.deleteByAcademyCodeAndToken(academyCode, refreshToken);
    }
}