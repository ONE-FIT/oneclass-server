package oneclass.oneclass.domain.sendon.sms.shortmessage;

import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.sendon.BaseScenario;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsResetPasswordCode extends BaseScenario {

    private final MemberRepository memberRepository;

    public void execute(String message, String phone) {

        SendSms sendSms = sendon.sms.sendSms(new SmsBuilder()
                .setFrom(SMS_MOBILE_FROM)
                .setTo(Collections.singletonList(phone))
                .setMessage(message)
                .setIsAd(false)
        );

        log.debug("응답: {}", gson.toJson(sendSms));
    }

    public String getDescription() {
        return "[SMS] 비밀번호 초기화 메세지 전송";
    }

    @Async
    public void send(String message, String phone) {
        execute(message, phone);
    }
}