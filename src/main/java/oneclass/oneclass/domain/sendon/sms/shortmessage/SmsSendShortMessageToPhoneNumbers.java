package oneclass.oneclass.domain.sendon.sms.shortmessage;

import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSendShortMessageToPhoneNumbers extends BaseScenario {

    public void execute(String message, String title, List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            log.warn("전화번호 리스트가 비어있습니다.");
            return;
        }

        try {
            SendSms sendSms = sendon.sms.sendSms(new SmsBuilder()
                    .setFrom(SMS_MOBILE_FROM)
                    .setTo(phoneNumbers)
                    .setMessage(title + "\n" + message)
                    .setIsAd(false)
            );

            log.info("SMS 발송 완료. 수신자 수: {}", phoneNumbers.size());
            log.debug("응답: {}", gson.toJson(sendSms));
        } catch (Exception e) {
            log.error("SMS 발송 중 오류 발생. 수신자 수: {}", phoneNumbers.size(), e);
            throw e;
        }
    }

    @Override
    public String getDescription() {
        return "[SMS] 전화번호 리스트로 즉시 문자 발송";
    }

    @Async
    public void send(String message, String title, List<String> phoneNumbers) {
        execute(message, title, phoneNumbers);
    }
}
