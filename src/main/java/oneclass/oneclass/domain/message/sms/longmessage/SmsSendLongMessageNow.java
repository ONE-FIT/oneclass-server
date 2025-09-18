package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessageAndTitle;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmsSendLongMessageNow extends BaseScenario implements ExecutableWithMessageAndTitle {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message, String title) {

    // In your service
    Pageable pageable = PageRequest.of(0, 1000); // 1번에 1000개 불러오기
    Page<String> phonePage;
    do {
      phonePage = memberRepository.findAllPhones(pageable);
      if (!phonePage.getContent().isEmpty()) {
        try {
          SendSms sendSms = sendon.sms.sendLms(
                  SMS_MOBILE_FROM,
                  phonePage.getContent(),
                  title,
                  message,
                  false,
                  null);
          log.debug("응답: {}", gson.toJson(sendSms));
        } catch (Exception e) {
          log.error("LMS 메시지 발송 중 오류 발생. Page: {}, Size: {}", phonePage.getNumber(), phonePage.getSize(), e);
        }
      }
      pageable = phonePage.nextPageable();
    } while (phonePage.hasNext());
  }

  @Override
  public String getDescription() {
    return "[LMS] 즉시문자 발송";
  }

  @Async
  public void send(String message, String title) {
    execute(message, title);
  }
}