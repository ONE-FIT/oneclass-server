package oneclass.oneclass.domain.sendon.sms.shortmessage;

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

@RequiredArgsConstructor
@Deprecated
public class SmsCancelShortMessageScenario extends BaseScenario implements ExecutableWithMessage {

    private final MemberRepository memberRepository;

    @Override
    public void execute(String message) {
        OffsetDateTime reservationTime = OffsetDateTime.now().plusMinutes(30);
        Reservation reservation = new Reservation(reservationTime.toString());
        SendSms sendSms = sendon.sms.sendSms(
                SMS_MOBILE_FROM,
                Arrays.asList(SMS_MOBILE_TO),
                message,
                true,
                reservation);
        Log.d("SendSms: " + gson.toJson(sendSms)); // sendon의 Log는 베포에 적합하지 않습니다.

        sleep(5000);

        CancelGroup cancelGroup = sendon.sms.cancelGroup(sendSms.data.groupId);
        Log.d("CancelGroup: " + gson.toJson(cancelGroup));
    }

    @Override
    public String getDescription() {
        return "[SMS] 예약문자 취소";
    }
}