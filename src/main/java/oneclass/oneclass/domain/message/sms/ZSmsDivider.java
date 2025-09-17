package oneclass.oneclass.domain.message.sms;

import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.Executable;

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
