package oneclass.oneclass.domain.sendon.kakao;

import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class ZKakaoDivider extends BaseScenario implements Executable {

    @Override
    public void execute() throws InterruptedException {
        System.out.println("Nothing to do");
    }

    @Override
    public String getDescription() {
        return "Kakao";
    }

    @Override
    public boolean isDivider() {
        return true;
    }
}