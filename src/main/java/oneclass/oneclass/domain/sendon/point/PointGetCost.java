package oneclass.oneclass.domain.sendon.point;

import io.sendon.Log;
import io.sendon.point.response.GetCosts;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class PointGetCost extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
    GetCosts getCosts  = sendon.point.getCosts();
    Log.d("GetCosts: " + gson.toJson(getCosts));
  }

  @Override
  public String getDescription() {
    return "[포인트] 단가 조회";
  }

}
