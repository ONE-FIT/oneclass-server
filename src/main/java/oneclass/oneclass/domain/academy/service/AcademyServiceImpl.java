package oneclass.oneclass.domain.academy.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.academy.dto.MadeRequest;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.entity.AcademyRefreshToken;
import oneclass.oneclass.domain.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.domain.academy.entity.Role;
import oneclass.oneclass.domain.academy.error.AuthError;
import oneclass.oneclass.domain.academy.repository.AcademyRefreshTokenRepository;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import oneclass.oneclass.domain.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.domain.member.dto.ResponseToken;
import oneclass.oneclass.domain.member.jwt.JwtProvider;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    //비번 랜덤생성
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
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

        // 비밀번호 자동 생성
        String randomPassword = generateRandomPassword(10); // 10자리 비밀번호
        Academy academy = new Academy();
        academy.setRole(role);
        academy.setAcademyCode(randomAcademyCode);
        academy.setAcademyName(request.getAcademyName());
        academy.setPassword(passwordEncoder.encode(randomPassword));
        log.info("학원 코드: " + randomAcademyCode);
        log.info("학원 평문 비밀번호: " + randomPassword); // 생성된 비밀번호 평문 로그

        academyRepository.save(academy);

    }

    @Override
    public ResponseToken login(String academyCode, String academyName , String password){
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));
        if (!academy.getAcademyName().equals(academyName)) {
            throw new CustomException(AuthError.NOT_FOUND);
        }
        if (!passwordEncoder.matches(password, academy.getPassword())) {
            throw new CustomException(AuthError.UNAUTHORIZED);
        }
        String roleClaim = "role" +academy.getRole();
        ResponseToken token = jwtProvider.generateToken(academy.getAcademyCode(), roleClaim);
        AcademyRefreshToken refreshToken = AcademyRefreshToken.builder()
                .academyCode(academyCode)
                .token(token.getRefreshToken())
                .expiryDate(LocalDateTime.now().plusDays(28))
                .build();

        academyRefreshTokenRepository.save(refreshToken);

        return token;

    }

    @Override
    public void sendResetPasswordEmail(String code, String name){
        Academy academy = academyRepository.findByAcademyCodeAndAcademyName(code , name)
                .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));
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
    public void resetPassword(String academyCode, String academyName, String verificationCode){
        // 인증코드 검증
        AcademyVerificationCode codeEntity = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));

        if (!codeEntity.getCode().equals(verificationCode)) {
            throw new CustomException(AuthError.UNAUTHORIZED);
        }
        // 인증코드 만료 검증
        if (codeEntity.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(AuthError.UNAUTHORIZED);
        }

        // 학원 정보 조회 및 이름 확인
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new CustomException(AuthError.NOT_FOUND));
        if (!academy.getAcademyName().equals(academyName)) {
            throw new CustomException(AuthError.NOT_FOUND);
        }

        // 임시 비밀번호 생성 및 변경
        String tempPassword = UUID.randomUUID().toString().substring(0, 10);
        academy.setPassword(passwordEncoder.encode(tempPassword));
        academyRepository.save(academy);

        // 인증코드 사용 완료 시 삭제
        academyVerificationCodeRepository.delete(codeEntity);

        String to = academy.getEmail();
        String subject = "임시 비밀번호 안내";
        String text = "임시 비밀번호: " + tempPassword;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
    }

}
