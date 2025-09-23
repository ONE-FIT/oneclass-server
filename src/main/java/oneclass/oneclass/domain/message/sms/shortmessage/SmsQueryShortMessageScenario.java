package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.response.GetGroup;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;

import java.util.Arrays;

@RequiredArgsConstructor
@Deprecated
public class SmsQueryShortMessageScenario extends BaseScenario implements ExecutableWithMessage {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message) {
    SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM,
            Arrays.asList(SMS_MOBILE_TO),
            message,
            true,
            null);
    Log.d("SendSms: " + gson.toJson(sendSms)); // sendon의 Log는 베포에 적합하지 않습니다.

    sleep(5000);

    GetGroup getGroup = sendon.sms.getGroup(sendSms.data.groupId);
    Log.d("GetGroup: " + gson.toJson(getGroup)); // sendon의 Log는 베포에 적합하지 않습니다.
  }

  @Override
  public String getDescription() {
    return "[SMS] 발송문자 조회";
  }
}