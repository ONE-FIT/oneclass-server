package oneclass.oneclass.domain.academy.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.academy.dto.MadeRequest;
import oneclass.oneclass.domain.academy.dto.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyRefreshToken;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.entity.Role;
import oneclass.oneclass.domain.academy.error.AcademyError;
import oneclass.oneclass.domain.academy.repository.AcademyRefreshTokenRepository;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.ResponseToken;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
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

    //랜덤한 코드를 만드는 함수(학원코드만들기용)
    public String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 사용할 문자 집합
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public void madeAcademy(MadeRequest request) {
        Role role = Role.ACADEMY;
        String randomAcademyCode;
        // 학원코드 중복 체크
        do {
            randomAcademyCode = generateRandomCode(8);
        } while (academyRepository.findByAcademyCode(randomAcademyCode).isPresent());

        // 비밀번호 일치 확인
        if (!request.getPassword().equals(request.getCheckPassword())) {
            throw new CustomException(AcademyError.PASSWORD_MISMATCH);
        }

        Academy academy = new Academy();
        academy.setRole(role);
        academy.setAcademyCode(randomAcademyCode);
        academy.setAcademyName(request.getAcademyName());
        academy.setEmail(request.getEmail());
        academy.setPassword(passwordEncoder.encode(request.getPassword()));
        log.info("학원 코드: {}", randomAcademyCode);


        academyRepository.save(academy);

    }

    @Override
    @Transactional
    public ResponseToken login(String academyCode, String academyName, String password) {
        //학원 검증
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!academy.getAcademyName().equals(academyName)) {
            throw new CustomException(AcademyError.NOT_FOUND);
        }
        if (!passwordEncoder.matches(password, academy.getPassword())) {
            throw new CustomException(AcademyError.UNAUTHORIZED);
        }

        String roleClaim = "ROLE_" + academy.getRole().name();

        //RefreshToken 조회
        Optional<AcademyRefreshToken> refreshOpt =
                academyRefreshTokenRepository.findByAcademyCode(academyCode);

        //유효한 RefreshToken이 있는 경우: AccessToken만 재발급 후 즉시 반환
        if (refreshOpt.isPresent() && !refreshOpt.get().isExpired()) {
            String accessToken = jwtProvider.generateAccessToken(academyCode, roleClaim);
            return new ResponseToken(accessToken, refreshOpt.get().getToken());
        }

        //만료되었거나 최초 발급: 새 pair 발급 + 저장 후 즉시 반환
        ResponseToken newPair = jwtProvider.generateToken(academyCode, roleClaim);

        AcademyRefreshToken tokenToSave = refreshOpt.orElseGet(() ->
                AcademyRefreshToken.builder()
                        .academyCode(academyCode)
                        .build()
        );

        tokenToSave.rotate(newPair.getRefreshToken(), LocalDateTime.now().plusDays(28));
        academyRefreshTokenRepository.save(tokenToSave);

        return new ResponseToken(newPair.getAccessToken(), newPair.getRefreshToken());
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
                .expiry(LocalDateTime.now().plusMinutes(10)) // 현재 시간 + 10분
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
        // 인증코드 검증
        AcademyVerificationCode codeEntity = academyVerificationCodeRepository.findByAcademyCode(request.getAcademyCode())
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));

        if (!codeEntity.getCode().equals(request.getVerificationCode())) {
            throw new CustomException(AcademyError.UNAUTHORIZED);
        }
        // 인증코드 만료 검증
        if (codeEntity.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(AcademyError.UNAUTHORIZED);
        }

        // 학원 정보 조회 및 이름 확인
        Academy academy = academyRepository.findByAcademyCode(request.getAcademyCode())
                .orElseThrow(() -> new CustomException(AcademyError.NOT_FOUND));
        if (!academy.getAcademyName().equals(request.getAcademyName())) {
            throw new CustomException(AcademyError.NOT_FOUND);
        }
            // 비밀번호 일치 확인
            if (!request.getNewPassword().equals(request.getCheckPassword())) {
                throw new CustomException(AcademyError.PASSWORD_MISMATCH);
            }
            academy.setPassword(passwordEncoder.encode(request.getNewPassword()));


        academyRepository.save(academy);

    }
    @Override
    @Transactional
    public void logout(String academyCode) {
        academyRefreshTokenRepository.findByAcademyCode(academyCode)
                .ifPresent(academyRefreshTokenRepository::delete);
    }

}
