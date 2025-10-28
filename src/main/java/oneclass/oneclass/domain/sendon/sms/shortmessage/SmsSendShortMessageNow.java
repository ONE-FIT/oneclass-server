package oneclass.oneclass.domain.sendon.sms.shortmessage;

import io.sendon.sms.request.MmsBuilder;
import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.ExecutableWithMessage;
import oneclass.oneclass.domain.sendon.ExecutableWithMessageTitleAndStudentIds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSendShortMessageNow extends BaseScenario implements ExecutableWithMessageTitleAndStudentIds {

    private final MemberRepository memberRepository;

    @Override
    public void execute(String message, String title, List<Long> studentIds) {
        // In your service
        Pageable pageable = PageRequest.of(0, 1000); // 1번에 1000개 불러오기
        Page<String> phonePage;
        do {
            phonePage = memberRepository.findPhonesByIds(studentIds, pageable);
            if (!phonePage.getContent().isEmpty()) {
                SendSms sendSms = sendon.sms.sendSms(new SmsBuilder()
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
    public void send(String message, String title, List<Long> studentIds) {
        execute(message, title, studentIds);
    }
}