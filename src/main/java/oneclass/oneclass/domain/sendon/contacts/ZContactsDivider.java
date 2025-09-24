
package oneclass.oneclass.domain.sendon.contacts;

import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

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
