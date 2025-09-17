package oneclass.oneclass.domain.message.sms.longmessage;

import io.sendon.Log;
import io.sendon.sms.request.MmsBuilder;
import io.sendon.sms.response.UploadImage;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.message.BaseScenario;
import oneclass.oneclass.domain.message.ExecutableWithMessageAndTitle;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class SmsSendLongMessageWithUploadImageScenario extends BaseScenario implements ExecutableWithMessageAndTitle {

  private final MemberRepository memberRepository;

  @Override
  public void execute(String message, String title) {
    List<File> images = Arrays.asList(new File("./img/aligo.png"));
    UploadImage uploadImage = sendon.sms.uploadImages(images);
    Log.d("UploadImage: " + gson.toJson(uploadImage));

    sendon.sms.sendMms(new MmsBuilder()
        .setFrom(SMS_MOBILE_FROM)
        .setTo(memberRepository.findAllPhones())
        .setTitle(title)
        .setMessage(message)
        .setImages(Arrays.asList(uploadImage.data.images.get(0).id))
        .setIsAd(true)
    );
  }

  @Override
  public String getDescription() {
    return "[MMS] 이미지 업로드 및 발송";
  }
}