package oneclass.oneclass.domain.message.kakao.template;

import io.sendon.Log;
import io.sendon.kakao.request.TemplateStatus;
import io.sendon.kakao.response.GetTemplates;
import oneclass.oneclass.domain.message.BaseScenario;

public class KakaoGetTemplateList extends BaseScenario {

  @Override
  public void execute() throws InterruptedException {
    GetTemplates  getTemplates = sendon.kakao.getTemplates(KKO_SEND_PROFILE_ID, null, 1, null, TemplateStatus.APPROVED, null);
    Log.d("GetTemplates: " + gson.toJson(getTemplates));
  }

  @Override
  public String getDescription() {
    return "[카카오] 템플릿 목록 조회";
  }

}