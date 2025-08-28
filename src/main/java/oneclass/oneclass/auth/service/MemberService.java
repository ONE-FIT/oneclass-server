package oneclass.oneclass.auth.service;

import oneclass.oneclass.auth.dto.ResponseToken;
import oneclass.oneclass.auth.dto.SignupRequest;

public interface MemberService {
    ResponseToken login(String username, String password);
    void signup(SignupRequest request);
    String findUsername(String emailOrPhone);
    void sendResetPassword(String usernameOrEmail);
    void resetPassword(String username , String newPassword, String verificationCode);
}
