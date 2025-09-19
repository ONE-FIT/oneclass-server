package oneclass.oneclass.domain.message.kakao.message;

import io.sendon.Log;
import io.sendon.kakao.request.FriendtalkBuilder;
import io.sendon.kakao.response.SendFriendtalk;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.Executable;

import java.util.Arrays;

/**
 * 친구톡 발송 (deprecated)
 */
@Deprecated
public class KakaoSendFriendTalk extends BaseScenario implements Executable {

    @Override
    public void execute() throws InterruptedException {
      SendFriendtalk friendtalk = sendon.kakao.sendFriendtalk(new FriendtalkBuilder()
          .setProfileId(KKO_CHANNEL_ID)
          .setTemplateId(KKO_TEMPLATE_ID)
          .setTo(Arrays.asList(KKO_MOBILE_TO))
          .setMessage("Hello, World!")
          .setIsAd(true)
      );
      Log.d("SendFriendtalk: " + gson.toJson(friendtalk));
    }

    @Override
    public String getDescription() {
        return "[카카오] 친구톡 발송";
    }

}
