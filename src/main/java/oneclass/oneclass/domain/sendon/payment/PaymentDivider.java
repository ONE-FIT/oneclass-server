package oneclass.oneclass.domain.sendon.payment;

import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class PaymentDivider extends BaseScenario implements Executable {

    @Override
    public void execute() throws InterruptedException {
        System.out.println("Nothing to do");
    }

    @Override
    public String getDescription() {
        return "Payment";
    }

    @Override
    public boolean isDivider() {
        return true;
    }
}