package oneclass.oneclass.global.auth.academy.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.academy.dto.AcademyLoginRequest;
import oneclass.oneclass.global.auth.academy.dto.MadeRequest;
import oneclass.oneclass.global.auth.academy.dto.ResetAcademyPasswordRequest;
import oneclass.oneclass.global.auth.academy.service.AcademyService;
import oneclass.oneclass.global.auth.member.dto.ResponseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/academy")
public class AcademyController {
    private final AcademyService academyService;

    //학원 계정 만들기
    @PostMapping("/new-academy")
    public ResponseEntity<Void> made(@RequestBody MadeRequest request) {
        academyService.madeAcademy(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseToken> login(@RequestBody AcademyLoginRequest request) {
        ResponseToken token = academyService.login(request.getAcademyCode(), request.getAcademyName(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    // 인증코드 발송
    @PostMapping("/send-reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(@RequestBody ResetAcademyPasswordRequest request) {
        academyService.sendResetPasswordEmail(request.getAcademyCode(), request.getAcademyName());
        return ResponseEntity.ok().build();
    }

    // 비밀번호 초기화
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetAcademyPasswordRequest request) {
        academyService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
