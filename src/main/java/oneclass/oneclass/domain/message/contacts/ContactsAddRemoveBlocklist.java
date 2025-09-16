package oneclass.oneclass.domain.message.contacts;

import io.sendon.Log;
import io.sendon.contacts.response.AddBlocklist;
import io.sendon.contacts.response.DeleteBlocklist;
import oneclass.oneclass.domain.message.BaseScenario;

public class ContactsAddRemoveBlocklist extends BaseScenario {

  @Override
  public void execute() throws InterruptedException {
      AddBlocklist addBlocklist = sendon.contacts.addBlocklist("01012345678");
      Log.d("AddBlocklist: " + gson.toJson(addBlocklist));

      DeleteBlocklist deleteBlocklist = sendon.contacts.deleteBlocklist(addBlocklist.data.id);
      Log.d("DeleteBlocklist: " + gson.toJson(deleteBlocklist));
  }

  @Override
  public String getDescription() {
    return "[주소록] 수신거부 추가삭제";
  }


}
