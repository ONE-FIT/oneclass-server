package oneclass.oneclass.auth.service;

import oneclass.oneclass.auth.dto.ResponseToken;
import oneclass.oneclass.auth.dto.SignupRequest;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {
    void signup(SignupRequest request); // 회원가입
    ResponseToken login(String username, String password); // 로그인
    String findUsername(String emailOrPhone); // 아이디 찾기
    void sendResetPasswordEmail(String usernameOrEmail); // 비번 재설정 메일 발송
    void resetPassword(String username, String newPassword, String verificationCode); // 비번 변경
}
