package oneclass.oneclass.domain.member.service;

import oneclass.oneclass.domain.member.dto.*;
import java.util.List;

public interface MemberService {
    void signup(SignupRequest request);
    ResponseToken login(String username, String password);
    String findUsername(String emailOrPhone);
    void sendResetPasswordEmail(String emailOrPhone);
    void resetPassword(String username, String newPassword, String verificationCode,  String checkPassword);

    // 로그아웃
    void logout(String username, String refreshToken); // 특정 refreshToken만 폐기(다중 세션)

    void sendSignupVerificationCode(String academyCode, String username);
    void addStudentsToParent(String username, String password, List<Long> studentIds);
    void deleteParent(Long parentId);

    //refreshToken 검증
    ResponseToken reissue(String refreshToken);
}