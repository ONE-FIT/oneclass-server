package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessageAndTitle;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SmsSendLongMessageNowScenario extends BaseScenario implements ExecutableWithMessageAndTitle {

  @Override
  public void execute(String message, String title) {
    SendSms sendSms = sendon.sms.sendLms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), title, message, true, null);
    Log.d("SendSms: " + gson.toJson(sendSms));
  }

  // TODO: 실제 유저의 번호를 적용시키는 방법은 정확히 모르겠습니다
  // TODO: Arrays.asList() 부분을 멤버 각각의 전화번호 필드에서 전화번호를 받은 후 넣어야 할 것으로 예상됩니다

  @Override
  public String getDescription() {
    return "[LMS] 즉시문자 발송";
  }
}