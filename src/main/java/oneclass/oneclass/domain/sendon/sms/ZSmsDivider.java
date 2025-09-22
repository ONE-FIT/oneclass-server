package oneclass.oneclass.domain.sendon.sms;

import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

public class ZSmsDivider extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
    System.out.println("Nothing to do");
  }

  @Override
  public String getDescription() {
    return "SMS";
  }

  @Override
  public boolean isDivider() {
    return true;
  }
}
