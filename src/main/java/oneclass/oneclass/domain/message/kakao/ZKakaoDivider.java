package oneclass.oneclass.domain.message.kakao;


import oneclass.oneclass.domain.message.BaseScenario;

public class ZKakaoDivider extends BaseScenario {

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
