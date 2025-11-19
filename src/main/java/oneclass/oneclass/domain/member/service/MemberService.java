package oneclass.oneclass.domain.member.service;

import oneclass.oneclass.domain.member.dto.request.LoginRequest;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
import oneclass.oneclass.domain.member.dto.response.TeacherStudentsResponse;

import java.util.List;

public interface MemberService {
    void signup(SignupRequest request);
    ResponseToken login(LoginRequest request);
    void resetPassword(String phone, String newPassword, String checkPassword, String verificationCode);

    // 로그아웃
    void logout(String username, String refreshToken); // 특정 refreshToken만 폐기(다중 세션)

    void sendSignupVerificationCode(String academyCode, String name);
    void addStudentsToParent(String parentUsername, String password, List<String> studentUsernames);
    void removeStudentsFromTeacher(String teacherPhone, List<String> studentPhones);
    TeacherStudentsResponse addStudentsToTeacher(String teacherPhone, List<String> studentPhones, String password);
    String cleanupToken(String token);

    //refreshToken 검증
    ResponseToken reissue(String refreshToken);

    // ---------- 조회 관련 서비스 메서드 추가 ----------
    /**
     * 요청자(requesterUsername)의 권한을 검사한 뒤,
     * teacherUsername이 맡고 있는 학생(username 리스트)를 반환한다.
     */
    java.util.List<String> listStudentsOfTeacher(String requesterPhone, String teacherPhone);

    /**
     * 요청자(requesterUsername)의 권한을 검사한 뒤,
     * studentUsername의 담당 교사(username 리스트)를 반환한다.
     */
    List<String> listTeachersOfStudent(String requesterPhone, String studentPhone);
}
