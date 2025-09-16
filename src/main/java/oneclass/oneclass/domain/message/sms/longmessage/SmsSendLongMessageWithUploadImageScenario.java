package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.request.MmsBuilder;
import io.sendon.sms.response.UploadImage;
import oneclass.oneclass.domain.message.BaseScenario;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SmsSendLongMessageWithUploadImageScenario extends BaseScenario {

  @Override
  public void execute() {
    List<File> images = Arrays.asList(new File("./img/aligo.png"));
    UploadImage uploadImage = sendon.sms.uploadImages(images);
    Log.d("UploadImage: " + gson.toJson(uploadImage));

    sendon.sms.sendMms(new MmsBuilder()
        .setFrom(SMS_MOBILE_FROM)
        .setTo(Arrays.asList(SMS_MOBILE_TO))
        .setTitle("Title")
        .setMessage("Hello, World!")
        .setImages(Arrays.asList(uploadImage.data.images.get(0).id))
        .setIsAd(true)
    );
  }

  @Override
  public String getDescription() {
    return "[MMS] 이미지 업로드 및 발송";
  }
}