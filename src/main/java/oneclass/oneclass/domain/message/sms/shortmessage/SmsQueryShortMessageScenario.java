package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.response.GetGroup;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.MessageBaseScenario;

import java.util.Arrays;

public class SmsQueryShortMessageScenario extends MessageBaseScenario {

  @Override
  public void execute(String message) {
    SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), message, true, null);
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