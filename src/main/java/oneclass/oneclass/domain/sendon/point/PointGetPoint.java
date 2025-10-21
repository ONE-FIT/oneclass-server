package oneclass.oneclass.domain.sendon.point;

import io.sendon.Log;
import io.sendon.point.response.GetPoints;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class PointGetPoint extends BaseScenario implements Executable {

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