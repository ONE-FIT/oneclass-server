package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.sms.request.MmsBuilder;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
public class SmsSendShortMessageNowScenario extends BaseScenario implements ExecutableWithMessage {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message) {
    // In your service
    Pageable pageable = PageRequest.of(0, 1000); // Process 1000 at a time
    Page<String> phonePage;
    do {
      phonePage = memberRepository.findAllPhones(pageable);
      if (!phonePage.getContent().isEmpty()) {
        sendon.sms.sendMms(new MmsBuilder()
                .setFrom(SMS_MOBILE_FROM)
                .setTo(phonePage.getContent())
                .setMessage(message)
                .setIsAd(false)
        );
      }
      pageable = phonePage.nextPageable();
    } while (phonePage.hasNext());
//    SendSms sendSms2 = sendon.sms.sendSms(new SmsBuilder()
//        .setFrom(SMS_MOBILE_FROM)
//        .setTo(memberRepository.findAllPhones())
//        .setMessage(message)
//        .setIsAd(false)
//    );
//    Log.d("응답: " + gson.toJson(sendSms2));
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