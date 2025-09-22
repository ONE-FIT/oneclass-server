package oneclass.oneclass.domain.sendon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import io.sendon.Sendon;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseScenario {

  protected static final int PHONE_PAGE_SIZE = 1000;

  private static final Dotenv dotenv = Dotenv.load();

  protected final String USER_ID;
  protected final String USER_APIKEY;
  protected final String SMS_MOBILE_TO;
  protected final String SMS_MOBILE_FROM;
  protected final String KKO_MOBILE_TO;
  protected final String KKO_TEMPLATE_ID;
  protected final String KKO_CHANNEL_ID;
  protected final String KKO_TOKEN_YOU_RECEIVED;
  protected final String KKO_SEND_PROFILE_ID;
  protected final String KKO_CHANNEL_PHONE_NUMBER;

  public abstract String getDescription();

  protected Sendon sendon;
  protected Gson gson;

  public BaseScenario() {
    USER_ID = dotenv.get("USER_ID");
    USER_APIKEY = dotenv.get("USER_APIKEY");
    SMS_MOBILE_TO = dotenv.get("SMS_MOBILE_TO");
    SMS_MOBILE_FROM = dotenv.get("SMS_MOBILE_FROM");
    KKO_MOBILE_TO = dotenv.get("KKO_MOBILE_TO");
    KKO_TEMPLATE_ID = dotenv.get("KKO_TEMPLATE_ID");
    KKO_CHANNEL_ID = dotenv.get("KKO_CHANNEL_ID");
    KKO_TOKEN_YOU_RECEIVED = dotenv.get("KKO_TOKEN_YOU_RECEIVED");
    KKO_SEND_PROFILE_ID = dotenv.get("KKO_SEND_PROFILE_ID");
    KKO_CHANNEL_PHONE_NUMBER = dotenv.get("KKO_CHANNEL_PHONE_NUMBER");

    sendon = Sendon.getInstance(USER_ID, USER_APIKEY, true);

    gson = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();
  }

  protected void handleException(Exception e) {
    log.error("에러 응답 발생", e);
  }

  protected static String generateRandomCode() {
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < 3; i++) code.append((char) (Math.random() * 26 + 'a'));
    return code.toString();
  }

  public boolean isDivider() {
    return false;
  }

  public void sleep(long ms) {
    Thread thread = new Thread(() -> {
      int seconds = 0;
      StringBuilder dots = new StringBuilder();
      try {
        while (true) {
          Thread.sleep(1000); // TODO: 다른 방법 사용하기 (현재는 사용되지는 않으나 나중에 sleep()이 사용될 시 주의)
          seconds++;
          dots.append("😎");
          log.debug("[{}]{}", seconds, dots);
        }
      } catch (InterruptedException e) {
        // Thread interrupted, stop printing
      }
    });

    thread.start();

    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      log.error("에러 응답 발생", e);
    }

    thread.interrupt();
    System.out.print("\n");
  }
}
