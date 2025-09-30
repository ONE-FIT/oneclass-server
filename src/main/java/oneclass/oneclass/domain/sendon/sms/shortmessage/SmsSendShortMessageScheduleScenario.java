package oneclass.oneclass.domain.sendon.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.request.Reservation;
import io.sendon.sms.request.SmsBuilder;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.ExecutableWithMessage;
import oneclass.oneclass.domain.member.repository.MemberRepository;

import java.util.Arrays;

@RequiredArgsConstructor
@Deprecated
public class SmsSendShortMessageScheduleScenario extends BaseScenario implements ExecutableWithMessage {

    private final MemberRepository memberRepository;

    @Override
    public void execute(String message) {
        Reservation reservation = new Reservation("2080-03-21 00:00:00"); // FIXME: 예약 전송이 하드코딩 되어있음
        SendSms sendSms = sendon.sms.sendSms(new SmsBuilder()
                .setFrom(SMS_MOBILE_FROM)
                .setTo(Arrays.asList(SMS_MOBILE_TO)) // 멤버의 휴대폰
                .setMessage(message)
                .setReservation(reservation)
                .setIsAd(true)
        );
        Log.d("SendSms: " + gson.toJson(sendSms)); // sendon의 Log는 베포에 적합하지 않습니다.
    }

    @Override
    public String getDescription() {
        return "[SMS] 예약문자 발송";
    }
}