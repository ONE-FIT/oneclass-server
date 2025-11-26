package oneclass.oneclass.domain.member.service;

import oneclass.oneclass.domain.member.dto.request.AdminSignupRequest;
import oneclass.oneclass.domain.member.dto.request.LoginRequest;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;

import java.util.List;

public interface MemberService {
    void signup(SignupRequest request);
    ResponseToken login(LoginRequest request);
    void resetPassword(String phone, String newPassword, String checkPassword, String verificationCode);
    void signupAdmin(AdminSignupRequest request); //관리자 회원가입
    // 로그아웃
    void logout(String username, String refreshToken); // 특정 refreshToken만 폐기(다중 세션)

    void sendSignupVerificationCode(String academyCode, String name);
    void addStudentsToParent(String parentUsername, String password, List<String> studentUsernames);
    void removeStudentsFromTeacher(String teacherPhone, List<String> studentPhones);
    TeacherStudentsResponse addStudentsToTeacher(String teacherPhone, List<String> studentPhones, String password);
    String cleanupToken(String token);

    //refreshToken 검증
    ResponseToken reissue(String refreshToken);
}
