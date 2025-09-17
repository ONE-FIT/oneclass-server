package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;

@RequiredArgsConstructor
public class SmsSendShortMessageNowScenario extends BaseScenario implements ExecutableWithMessage {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message) {
    SendSms sendSms2 = sendon.sms.sendSms(new SmsBuilder()
        .setFrom(SMS_MOBILE_FROM)
        .setTo(memberRepository.findAllPhones())
        .setMessage(message)
        .setIsAd(false)
    );
    Log.d("응답: " + gson.toJson(sendSms2));
  }

  @Override
  public String getDescription() {
    return "[SMS] 즉시문자 발송";
  }
}