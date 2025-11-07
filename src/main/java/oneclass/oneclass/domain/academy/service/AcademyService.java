package oneclass.oneclass.domain.academy.service;

import oneclass.oneclass.domain.academy.dto.response.MadeAcademyResponse;
import oneclass.oneclass.domain.academy.dto.request.MadeRequest;
import oneclass.oneclass.domain.academy.dto.request.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.member.dto.response.TokenResponse;

public interface AcademyService {
    MadeAcademyResponse madeAcademy(MadeRequest request);
    TokenResponse login(String academyCode, String academyName, String password);
    void sendResetPasswordEmail(String code, String name);
    void resetPassword(ResetAcademyPasswordRequest request);

    // 로그아웃: 특정 refresh 토큰만 폐기
    void logout(String academyCode, String refreshToken);
}