package oneclass.oneclass.domain.message.kakao.template;

import io.sendon.Log;
import io.sendon.kakao.response.GetTemplateDetail;
import oneclass.oneclass.domain.message.BaseScenario;

public class KakaoGetTemplate extends BaseScenario {

  @Override
  public void execute() throws InterruptedException {
    GetTemplateDetail getTemplateDetail = sendon.kakao.getTemplateDetail(KKO_SEND_PROFILE_ID, KKO_TEMPLATE_ID);
    Log.d("GetTemplateDetail: " + gson.toJson(getTemplateDetail));
  }

  @Override
  public String getDescription() {
    return "[카카오] 템플릿 조회";
  }

}