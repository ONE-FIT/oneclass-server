package oneclass.oneclass.domain.member.service;

import oneclass.oneclass.domain.member.dto.response.TokenResponse;
import oneclass.oneclass.domain.member.dto.request.SignupRequest;

import java.util.List;

public interface MemberService {
    void signup(SignupRequest request);
    TokenResponse login(String phone, String password);
    String findUsername(String phone);
    void resetPassword(String phone, String newPassword, String checkPassword, String verificationCode);

    // 로그아웃
    void logout(String phone, String refreshToken); // 특정 refreshToken만 폐기(다중 세션)

    void sendSignupVerificationCode(String academyCode, String username);
    void addStudentsToParent(String username, String password, List<String> studentUsername);
    void deleteParent(Long parentId);
    void removeStudentsFromTeacher(String teacherUsername, List<String> studentUsernames);
    void addStudentsToTeacher(String teacherUsername, List<String> studentUsernames, String password);
    void createUsername(String username);
    void deleteUser(String phone);

    //refreshToken 검증
    TokenResponse reissue(String refreshToken);

    // ---------- 조회 관련 서비스 메서드 추가 ----------
    /**
     * 요청자(requesterUsername)의 권한을 검사한 뒤,
     * teacherUsername이 맡고 있는 학생(username 리스트)를 반환한다.
     */
    java.util.List<String> listStudentsOfTeacher(String requesterUsername, String teacherUsername);

    /**
     * 요청자(requesterUsername)의 권한을 검사한 뒤,
     * studentUsername의 담당 교사(username 리스트)를 반환한다.
     */
    List<String> listTeachersOfStudent(String requesterUsername, String studentUsername);
}