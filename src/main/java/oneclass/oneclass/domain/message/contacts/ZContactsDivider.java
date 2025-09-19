package oneclass.oneclass.domain.message.contacts;

import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.Executable;

@Deprecated
public class ZContactsDivider extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
    System.out.println("Nothing to do");
  }

  @Override
  public String getDescription() {
    return "Contacts";
  }

  @Override
  public boolean isDivider() {
    return true;
  }
}
