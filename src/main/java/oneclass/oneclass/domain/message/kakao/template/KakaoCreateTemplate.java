package oneclass.oneclass.domain.message.kakao.template;

import io.sendon.Log;
import io.sendon.kakao.request.Button;
import io.sendon.kakao.request.ButtonType;
import io.sendon.kakao.request.Template;
import io.sendon.kakao.request.WlButton;
import io.sendon.kakao.response.CreateTemplate;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.Executable;

@Deprecated
public class KakaoCreateTemplate extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
    Button button = new WlButton()
      .setName("웹사이트 바로가기")
      .setType(ButtonType.WL)
      .setUrlMobile("http://example.com")
      .setUrlPc("http://example.com");

    CreateTemplate createTemplate = sendon.kakao.createTemplate(
        "alipeople",
        new Template()
          .setTemplateName("NewTemplate")
          .setTemplateContent("NewTemplateContent")
          .addButton(button)
    );
    Log.d("CreateTemplate: " + gson.toJson(createTemplate));
  }


  @Override
  public String getDescription() {
    return "[카카오] 템플릿 생성";
  }

}