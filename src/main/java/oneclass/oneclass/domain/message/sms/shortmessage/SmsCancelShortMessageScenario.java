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

@RequiredArgsConstructor
public class SmsCancelShortMessageScenario extends BaseScenario implements ExecutableWithMessage {


  private final MemberRepository memberRepository;

  @Override
  public void execute(String message) {
    OffsetDateTime reservationTime = OffsetDateTime.now().plusMinutes(30);
    Reservation reservation = new Reservation(reservationTime.toString());
    SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM, memberRepository.findAllPhones(), message, true, reservation);
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