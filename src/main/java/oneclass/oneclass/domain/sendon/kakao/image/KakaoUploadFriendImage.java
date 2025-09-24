package oneclass.oneclass.domain.sendon.kakao.image;

import io.sendon.Log;
import io.sendon.kakao.response.UploadFriendtalkImage;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

import java.io.File;

/**
 * 친구톡 이미지 업로드 (deprecated)
 */
@Deprecated
public class KakaoUploadFriendImage extends BaseScenario implements Executable {

    @Override
    public void execute() {
        File file = new File("./img/aligo.png");
        UploadFriendtalkImage uploadFriendtalkImage = sendon.kakao.uploadFriendtalkImage(file);
        Log.d("UploadFriendtalkImage: " + gson.toJson(uploadFriendtalkImage));
    }

    @Override
    public String getDescription() {
        return "[카카오] 친구톡 이미지 업로드";
    }

}