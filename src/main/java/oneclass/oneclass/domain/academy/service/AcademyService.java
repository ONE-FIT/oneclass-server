package oneclass.oneclass.domain.academy.service;

import oneclass.oneclass.domain.academy.dto.MadeRequest;
import oneclass.oneclass.domain.academy.dto.ResetAcademyPasswordRequest;
import oneclass.oneclass.domain.member.dto.ResponseToken;

public interface AcademyService {
    ResponseToken login(String academyCode, String academyName ,String password);
    void resetPassword(ResetAcademyPasswordRequest request);
    void sendResetPasswordEmail(String academyCode, String academyName);
    void madeAcademy(MadeRequest request);
    void logout(String academyCode);
}
