package oneclass.oneclass.domain.academy.service;

import oneclass.oneclass.domain.academy.dto.request.AcademySignupRequest;
import oneclass.oneclass.domain.academy.dto.request.ApproveAcademyRequest;
import oneclass.oneclass.domain.academy.dto.request.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.academy.dto.response.AcademySignupResponse;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;

public interface AcademyService {
    AcademySignupResponse academySignup(AcademySignupRequest request);
    ResponseToken login(String academyCode, String academyName, String password);
    void sendResetPasswordEmail(String code, String name);
    void resetPassword(ResetAcademyPasswordRequest request);

    // 로그아웃: 특정 refresh 토큰만 폐기
    void logout(String academyCode, String refreshToken);

    void approveAcademy(ApproveAcademyRequest request);
}