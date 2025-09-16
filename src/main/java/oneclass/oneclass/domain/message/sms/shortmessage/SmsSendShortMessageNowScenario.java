package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessage;

import java.util.Arrays;

public class SmsSendShortMessageNowScenario extends BaseScenario implements ExecutableWithMessage {

  @Override
  public void execute(String message) {
    SendSms sendSms2 = sendon.sms.sendSms(new SmsBuilder()
        .setFrom(SMS_MOBILE_FROM)
        .setTo(Arrays.asList(SMS_MOBILE_TO))
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