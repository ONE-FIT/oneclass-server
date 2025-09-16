package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.MessageBaseScenario;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SmsSendLongMessageNowScenario extends MessageBaseScenario {

  // TODO: title이 하드코딩 된 것으로 확인되었습니다.
  // TODO: 추후 공지사항의 title을 받아 넣어야 합니다.

  @Override
  public void execute(String message) {
    SendSms sendSms = sendon.sms.sendLms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), "Title", message, true, null);
    Log.d("SendSms: " + gson.toJson(sendSms));
  }

  @Override
  public String getDescription() {
    return "[LMS] 즉시문자 발송";
  }
}