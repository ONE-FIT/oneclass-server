package oneclass.oneclass.domain.academy.service;

import oneclass.oneclass.domain.academy.dto.MakeAcademyResponse;
import oneclass.oneclass.domain.academy.dto.MakeAcademyRequest;
import oneclass.oneclass.domain.academy.dto.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.member.dto.ResponseToken;

public interface AcademyService {
    MakeAcademyResponse makeAcademy(MakeAcademyRequest request);
    ResponseToken login(String academyCode, String academyName, String password);
    void sendResetPasswordEmail(String code, String name);
    void resetPassword(ResetAcademyPasswordRequest request);

    // 로그아웃: 특정 refresh 토큰만 폐기
    void logout(String academyCode, String refreshToken);
}