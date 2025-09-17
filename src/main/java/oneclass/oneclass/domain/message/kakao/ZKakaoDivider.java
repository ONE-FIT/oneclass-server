package oneclass.oneclass.domain.message.kakao;


import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.Executable;

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
