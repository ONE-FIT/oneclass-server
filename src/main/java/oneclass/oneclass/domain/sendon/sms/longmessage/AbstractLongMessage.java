package oneclass.oneclass.domain.sendon.sms.longmessage;

import io.sendon.sms.response.SendSms;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.ExecutableWithMessageAndTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public abstract class AbstractLongMessage extends BaseScenario implements ExecutableWithMessageAndTitle {

    @Override
    public void execute(String message, String title){
        Pageable pageable = PageRequest.of(0, PHONE_PAGE_SIZE);
        Page<String> phonePage;

        do {
            phonePage = findTargets(pageable); // 추상 메서드로 대상 가져오기
            if (!phonePage.getContent().isEmpty()) {
                try {
                    SendSms sendSms = sendon.sms.sendLms(
                            SMS_MOBILE_FROM,
                            phonePage.getContent(),
                            title,
                            message,
                            false,
                            null
                    );
                    log.debug("응답: {}", gson.toJson(sendSms));
                } catch (Exception e) {
                    log.error("LMS 메시지 발송 중 오류 발생. Page: {}, Size: {}", phonePage.getNumber(), phonePage.getSize(), e);
                }
            }
            pageable = phonePage.nextPageable();
        } while (phonePage.hasNext());
    }

    protected abstract Page<String> findTargets(Pageable pageable); // 달라지는 부분만 서브클래스에서 구현

    @Async
    public void send(String message, String title) {
        execute(message, title);
    }
}