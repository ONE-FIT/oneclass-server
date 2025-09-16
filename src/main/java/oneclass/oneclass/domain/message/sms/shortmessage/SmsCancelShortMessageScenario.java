package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.request.Reservation;
import io.sendon.sms.response.CancelGroup;
import io.sendon.sms.response.SendSms;
import oneclass.oneclass.domain.message.BaseScenario;

import java.time.OffsetDateTime;
import java.util.Arrays;
public class SmsCancelShortMessageScenario extends BaseScenario {

  @Override
  public void execute() {
    OffsetDateTime reservationTime = OffsetDateTime.now().plusMinutes(30);
    Reservation reservation = new Reservation(reservationTime.toString());
    SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), "Hello, World!", true, reservation);
    Log.d("SendSms: " + gson.toJson(sendSms));

    sleep(5000);

    CancelGroup cancelGroup = sendon.sms.cancelGroup(sendSms.data.groupId);
    Log.d("CancelGroup: " + gson.toJson(cancelGroup));
  }

  @Override
  public String getDescription() {
    return "[SMS] 예약문자 취소";
  }
}