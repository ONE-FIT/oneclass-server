package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.response.GetGroup;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;

@RequiredArgsConstructor
public class SmsQueryShortMessageScenario extends BaseScenario implements ExecutableWithMessage {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message) {
    SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM,
            memberRepository.findAllPhones(),
            message,
            true,
            null);
    Log.d("SendSms: " + gson.toJson(sendSms));

    sleep(5000);

    GetGroup getGroup = sendon.sms.getGroup(sendSms.data.groupId);
    Log.d("GetGroup: " + gson.toJson(getGroup));
  }

  @Override
  public String getDescription() {
    return "[SMS] 발송문자 조회";
  }
}