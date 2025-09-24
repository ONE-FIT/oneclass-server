package oneclass.oneclass.domain.sendon.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.request.Reservation;
import io.sendon.sms.response.CancelGroup;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.ExecutableWithMessage;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;

import java.time.OffsetDateTime;
import java.util.Arrays;

@Deprecated
@RequiredArgsConstructor
public class SmsCancelLongMessageScenario extends BaseScenario implements ExecutableWithMessage {

  private final MemberRepository memberRepository;

  @Deprecated
  @Override
  public void execute(String message) {
    OffsetDateTime reservationTime = OffsetDateTime.now().plusMinutes(60);
    Reservation reservation = new Reservation(reservationTime.toString());
    SendSms sendSms = sendon.sms.sendLms(SMS_MOBILE_FROM, Arrays.asList(SMS_MOBILE_TO), "Title", message, true, reservation);
    Log.d("SendSms: " + gson.toJson(sendSms));

    sleep(5000);

    CancelGroup cancelGroup = sendon.sms.cancelGroup(sendSms.data.groupId);
    Log.d("CancelGroup: " + gson.toJson(cancelGroup));

  }

  @Override
  public String getDescription() {
    return "[LMS] 예약문자 취소";
  }
}
