package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.request.Reservation;
import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.BaseScenario;

import java.util.Arrays;

public class SmsSendShortMessageScheduleScenario extends BaseScenario {

  @Override
  public void execute() {
    Reservation reservation = new Reservation("2080-03-21 00:00:00");
    SendSms sendSms = sendon.sms.sendSms(new SmsBuilder()
        .setFrom(SMS_MOBILE_FROM)
        .setTo(Arrays.asList(SMS_MOBILE_TO))
        .setMessage("Hello, World!")
        .setReservation(reservation)
        .setIsAd(true)
    );
    Log.d("SendSms: " + gson.toJson(sendSms));
  }


  @Override
  public String getDescription() {
    return "[SMS] 예약문자 발송";
  }
}