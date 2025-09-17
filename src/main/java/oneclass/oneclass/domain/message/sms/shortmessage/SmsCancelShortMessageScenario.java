package oneclass.oneclass.domain.message.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.request.Reservation;
import io.sendon.sms.response.CancelGroup;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;

import java.time.OffsetDateTime;
import java.util.Arrays;

@RequiredArgsConstructor
public class SmsCancelShortMessageScenario extends BaseScenario implements ExecutableWithMessage {

  private final MemberRepository memberRepository;

  @Override
  @Deprecated
  public void execute(String message) {
    OffsetDateTime reservationTime = OffsetDateTime.now().plusMinutes(30);
    Reservation reservation = new Reservation(reservationTime.toString());
    SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), message, true, reservation);
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