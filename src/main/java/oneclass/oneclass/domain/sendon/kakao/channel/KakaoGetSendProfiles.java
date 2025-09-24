package oneclass.oneclass.domain.sendon.kakao.channel;

import io.sendon.Log;
import io.sendon.kakao.response.GetProfiles;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class KakaoGetSendProfiles extends BaseScenario implements Executable {

    @Override
    public void execute() {
        GetProfiles getProfiles = sendon.kakao.getProfiles(2, null);
        Log.d("GetProfiles: " + gson.toJson(getProfiles));
    }

    @Override
    public String getDescription() {
        return "[카카오] 발신프로필 리스트 조회";
    }
}