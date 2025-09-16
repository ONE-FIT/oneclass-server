package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.response.GetGroup;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.MessageBaseScenario;

import java.util.Arrays;

public class SmsQueryLongMessageScenario extends MessageBaseScenario {

  @Override
  public void execute(String message) {
    SendSms sendSms = sendon.sms.sendLms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), "Title", message, true, null);
    Log.d("SendSms: " + gson.toJson(sendSms));

    sleep(5000);

    GetGroup getGroup = sendon.sms.getGroup(sendSms.data.groupId);
    Log.d("GetGroup: " + gson.toJson(getGroup));
  }

  @Override
  public String getDescription() {
    return "[LMS] 발송문자 조회";
  }
}