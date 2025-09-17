package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessageAndTitle;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsSendLongMessageNowScenario extends BaseScenario implements ExecutableWithMessageAndTitle {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message, String title) {
    SendSms sendSms = sendon.sms.sendLms(SMS_MOBILE_FROM, memberRepository.findAllPhones(), title, message, true, null);
    Log.d("SendSms: " + gson.toJson(sendSms));
  }

  @Override
  public String getDescription() {
    return "[LMS] 즉시문자 발송";
  }
}