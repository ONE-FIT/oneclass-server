package oneclass.oneclass.global.auth.academy.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.global.auth.academy.dto.MadeRequest;
import oneclass.oneclass.global.auth.academy.entity.Academy;
import oneclass.oneclass.global.auth.academy.entity.AcademyRefreshToken;
import oneclass.oneclass.global.auth.academy.entity.AcademyVerificationCode;
import oneclass.oneclass.global.auth.academy.entity.Role;
import oneclass.oneclass.global.auth.academy.repository.AcademyRepository;
import oneclass.oneclass.global.auth.academy.repository.AcademyRefreshTokenRepository;
import oneclass.oneclass.global.auth.academy.repository.AcademyVerificationCodeRepository;
import oneclass.oneclass.global.auth.member.dto.ResponseToken;
import oneclass.oneclass.global.auth.member.jwt.JwtProvider;
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학원코드입니다"));

        if (!academy.getAcademyName().equals(academyName)) {
            throw new IllegalArgumentException("학원코드와 학원이름이 일치하지 않습니다.");
        }
        if (!academy.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("학원 정보를 찾을 수 없습니다."));
        String academyCode = academy.getAcademyCode();
        String tempCode = UUID.randomUUID().toString().substring(0, 13);

        AcademyVerificationCode verificationCode = AcademyVerificationCode.builder()
                .academyCode(academyCode)
                .code(tempCode)
                .expiry(LocalDateTime.now().plusMinutes(10)) // 현재 시간 + 10분
                .build();

        academyVerificationCodeRepository.save(verificationCode);

    }

    @Override
    public void resetPassword(String academyCode, String academyName, String verificationCode){
        // 인증코드 검증
        AcademyVerificationCode codeEntity = academyVerificationCodeRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드가 존재하지 않습니다."));

        if (!codeEntity.getCode().equals(verificationCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        // 인증코드 만료 검증
        if (codeEntity.getExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다.");
        }


        // 학원 정보 조회 및 이름 확인
        Academy academy = academyRepository.findByAcademyCode(academyCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학원코드입니다."));
        if (!academy.getAcademyName().equals(academyName)) {
            throw new IllegalArgumentException("학원이름이 일치하지 않습니다.");
        }

        // 임시 비밀번호 생성 및 변경
        String tempPassword = UUID.randomUUID().toString().substring(0, 10);
        academy.setPassword(passwordEncoder.encode(tempPassword));
        academyRepository.save(academy);

        //인증코드 사용 완료 시 삭제
        academyVerificationCodeRepository.delete(codeEntity);

    }

}
