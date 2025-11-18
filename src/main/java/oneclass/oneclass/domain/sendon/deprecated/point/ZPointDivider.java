package oneclass.oneclass.domain.sendon.deprecated.point;

import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class ZPointDivider extends BaseScenario implements Executable {

    @Override
    public void execute() throws InterruptedException {
        System.out.println("Nothing to do");
    }

    @Override
    public String getDescription() {
        return "Point";
    }

    @Override
    public boolean isDivider() {
        return true;
    }
}