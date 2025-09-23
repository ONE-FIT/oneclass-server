package oneclass.oneclass.domain.sendon.payment;

import io.sendon.Log;
import io.sendon.payment.response.GetPaymentHistories;
import io.sendon.payment.response.GetPaymentHistory;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class PaymentGetHistories extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
    GetPaymentHistories getPaymentHistories = sendon.payment.getPaymentHistories(1, 10, "2021-01-01", "2025-12-31");
    Log.d("GetPaymentHistories: " + gson.toJson(getPaymentHistories));

    GetPaymentHistory getPaymentHistory = sendon.payment.getPaymentHistory(getPaymentHistories.data.histories.get(0).id);
    Log.d("GetPaymentHistory: " + gson.toJson(getPaymentHistory));
  }

  @Override
  public String getDescription() {
    return "[결제] 결제 내역 조회";
  }

}
