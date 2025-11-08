package oneclass.oneclass.domain.sendon.sms.shortmessage;

import io.sendon.Log;
import io.sendon.sms.response.GetGroup;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.ExecutableWithMessage;

import java.util.Arrays;

@RequiredArgsConstructor
@Deprecated
public class SmsQueryShortMessageScenario extends BaseScenario {

    private final MemberRepository memberRepository;

    public void execute(String message) {
        SendSms sendSms = sendon.sms.sendSms(SMS_MOBILE_FROM,
                Arrays.asList(SMS_MOBILE_TO),
                message,
                true,
                null);
        Log.d("SendSms: " + gson.toJson(sendSms)); // sendon의 Log는 베포에 적합하지 않습니다.

        sleep(5000);

        GetGroup getGroup = sendon.sms.getGroup(sendSms.data.groupId);
        Log.d("GetGroup: " + gson.toJson(getGroup)); // sendon의 Log는 베포에 적합하지 않습니다.
    }

    @Override
    public String getDescription() {
        return "[SMS] 발송문자 조회";
    }
}