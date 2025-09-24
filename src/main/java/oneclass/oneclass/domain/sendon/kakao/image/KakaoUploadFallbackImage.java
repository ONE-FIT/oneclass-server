package oneclass.oneclass.domain.sendon.kakao.image;

import io.sendon.Log;
import io.sendon.kakao.response.UploadFallbackImage;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.domain.sendon.Executable;

import java.io.File;
import java.util.Arrays;

@Deprecated
public class KakaoUploadFallbackImage extends BaseScenario implements Executable {

    @Override
    public void execute() {
        File file = new File("./img/aligo.png");
        UploadFallbackImage uploadFallbackImage = sendon.kakao.uploadFallbackImage(Arrays.asList(file));
        Log.d("UploadFallbackImage: " + gson.toJson(uploadFallbackImage));
    }

    @Override
    public String getDescription() {
        return "[카카오] 대체문자 이미지 업로드";
    }

}