package oneclass.oneclass.domain.sendon.deprecated.sms;

import io.sendon.Log;
import io.sendon.sms.response.GetGroup;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.sendon.BaseScenario;

import java.util.Arrays;

@RequiredArgsConstructor
@Deprecated
public class SmsQueryLongMessageScenario extends BaseScenario {

    private final MemberRepository memberRepository;

    public void execute(String message, String title) {
        SendSms sendSms = sendon.sms.sendLms(
                SMS_MOBILE_FROM,
                Arrays.asList(SMS_MOBILE_TO),
                title,
                message, true,
                null);
        Log.d("SendSms: " + gson.toJson(sendSms)); // sendon의 Log는 베포에 적합하지 않습니다.

        sleep(5000);

        GetGroup getGroup = sendon.sms.getGroup(sendSms.data.groupId);
        Log.d("GetGroup: " + gson.toJson(getGroup)); // sendon의 Log는 베포에 적합하지 않습니다.
    }

    @Override
    public String getDescription() {
        return "[LMS] 발송문자 조회";
    }

}