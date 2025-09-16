package oneclass.oneclass.domain.message.sender;

import io.sendon.Log;
import io.sendon.sender.response.GetUserNumbers;
import oneclass.oneclass.domain.message.BaseScenario;

public class SenderGetNumbers extends BaseScenario {

  @Override
  public void execute() throws InterruptedException {
    GetUserNumbers getUserNumbers = sendon.sender.getUserNumbers(0, 10);
    Log.d("GetUserNumbers: " + gson.toJson(getUserNumbers));
  }

  @Override
  public String getDescription() {
    return "[발신번호] 발신번호 목록조회";
  }
}
