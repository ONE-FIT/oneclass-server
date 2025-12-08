package oneclass.oneclass.domain.member.service;

import oneclass.oneclass.domain.member.dto.request.AdminSignupRequest;
import oneclass.oneclass.domain.member.dto.request.LoginRequest;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    void signup(SignupRequest request);
    ResponseToken login(LoginRequest request);
    void resetPassword(String phone, String verificationCode, String newPassword, String checkPassword);
    void signupAdmin(AdminSignupRequest request); //관리자 회원가입
    // 로그아웃
    void logout(String username, String refreshToken); // 특정 refreshToken만 폐기(다중 세션)

    void sendSignupVerificationCode(String academyCode, String name);
    void addStudentsToParent(String parentUsername, String password, List<String> studentUsernames);
    void removeStudentsFromTeacher(String teacherPhone, List<String> studentPhones,Authentication authentication);
    TeacherStudentsResponse addStudentsToTeacher(String teacherPhone, List<String> studentPhones, String password);
    String cleanupToken(String token);
    //refreshToken 검증
    ResponseToken reissue(String refreshToken);

    /**
     * 요청자(requesterUsername)의 권한을 검사한 뒤,
     * studentUsername의 담당 교사(username 리스트)를 반환한다.
     */
    Optional<Long> findMemberIdByUsername(String username);
}
