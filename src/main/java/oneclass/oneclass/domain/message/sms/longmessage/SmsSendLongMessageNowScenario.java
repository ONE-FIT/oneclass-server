package oneclass.oneclass.domain.message.sms.longmessage;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessageAndTitle;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsSendLongMessageNowScenario extends BaseScenario implements ExecutableWithMessageAndTitle {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message, String title) {

    // In your service
    Pageable pageable = PageRequest.of(0, 1000); // Process 1000 at a time
    Page<String> phonePage;
    do {
      phonePage = memberRepository.findAllPhones(pageable);
      if (!phonePage.getContent().isEmpty()) {
        sendon.sms.sendLms(SMS_MOBILE_FROM, phonePage.getContent(), title, message, false, null);
      }
      pageable = phonePage.nextPageable();
    } while (phonePage.hasNext());

//    SendSms sendSms = sendon.sms.sendLms(SMS_MOBILE_FROM, memberRepository.findAllPhones(), title, message, true, null);
//    Log.d("SendSms: " + gson.toJson(sendSms));
  }

  @Override
  public String getDescription() {
    return "[LMS] 즉시문자 발송";
  }

  @Async
  public void smsSendLongMessageNow(String message, String title) {
    execute(message, title);
  }
}