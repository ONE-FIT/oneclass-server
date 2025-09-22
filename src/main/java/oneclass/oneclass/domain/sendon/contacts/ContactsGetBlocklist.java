package oneclass.oneclass.domain.sendon.contacts;

import io.sendon.Log;
import io.sendon.contacts.response.GetBlocklist;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

@Deprecated
public class ContactsGetBlocklist extends BaseScenario implements Executable {

  @Override
  public void execute() throws InterruptedException {
      GetBlocklist getBlocklist = sendon.contacts.getBlocklist(0, 10);
      Log.d("GetBlocklist: " + gson.toJson(getBlocklist));
  }

  @Override
  public String getDescription() {
    return "[주소록] 차단목록 조회";
  }


}
