package oneclass.oneclass.domain.message.point;

import io.sendon.Log;
import io.sendon.point.response.GetPoints;
import oneclass.oneclass.domain.message.BaseScenario;

public class PointGetPoint extends BaseScenario {

  @Override
  public void execute() throws InterruptedException {
    GetPoints getPoints = sendon.point.getPoints();
    Log.d("GetPoints: " + gson.toJson(getPoints));
  }

  @Override
  public String getDescription() {
    return "[포인트] 포인트 조회";
  }

}
