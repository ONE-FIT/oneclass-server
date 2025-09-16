package oneclass.oneclass.domain.message.point;

import oneclass.oneclass.domain.message.BaseScenario;

public class ZPointDivider extends BaseScenario {

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
