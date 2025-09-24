package oneclass.oneclass.domain.sendon.kakao.message;

import io.sendon.Log;
import io.sendon.kakao.request.AlimtalkBuilder;
import io.sendon.kakao.response.SendAlimtalk;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

import java.util.Arrays;

@Deprecated
public class KakaoSendAlimTalk extends BaseScenario implements Executable {

    @Override
    public void execute() throws InterruptedException {
        SendAlimtalk sendAlimtalkResult = sendon.kakao.sendAlimtalk(new AlimtalkBuilder()
                .setProfileId(KKO_SEND_PROFILE_ID)
                .setTemplateId(KKO_TEMPLATE_ID)
                .setTo(Arrays.asList(KKO_MOBILE_TO))
        );
        Log.d("SendAlimtalk: " + gson.toJson(sendAlimtalkResult));
    }

    @Override
    public String getDescription() {
        return "[카카오] 알림톡 발송";
    }

}