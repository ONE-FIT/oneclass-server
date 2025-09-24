package oneclass.oneclass.domain.sendon.sms.shortmessage;

import io.sendon.sms.request.MmsBuilder;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

@Deprecated
@Slf4j
@RequiredArgsConstructor
public class SmsSendShortMessageNow extends BaseScenario implements ExecutableWithMessage {

    private final MemberRepository memberRepository;

    @Override
    public void execute(String message) {
        // In your service
        Pageable pageable = PageRequest.of(0, 1000); // 1번에 1000개 불러오기
        Page<String> phonePage;
        do {
            phonePage = memberRepository.findAllPhones(pageable);
            if (!phonePage.getContent().isEmpty()) {
                SendSms sendSms = sendon.sms.sendMms(new MmsBuilder()
                        .setFrom(SMS_MOBILE_FROM)
                        .setTo(phonePage.getContent())
                        .setMessage(message)
                        .setIsAd(false)
                );

                log.debug("응답: {}", gson.toJson(sendSms));
            }
            pageable = phonePage.nextPageable();
        } while (phonePage.hasNext());
    }

    @Override
    public String getDescription() {
        return "[SMS] 즉시문자 발송";
    }

    @Async
    public void send(String message) {
        execute(message);
    }
}