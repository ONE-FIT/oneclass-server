package oneclass.oneclass.domain.message.sender;

import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.Executable;

public class ZSenderDivider extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
    System.out.println("Nothing to do");
  }

  @Override
  public String getDescription() {
    return "Sender";
  }

  @Override
  public boolean isDivider() {
    return true;
  }
}
