package oneclass.oneclass.domain.message.sender;

import oneclass.oneclass.domain.message.BaseScenario;

public class ZSenderDivider extends BaseScenario {

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
