package oneclass.oneclass.domain.sendon.kakao.message;

import io.sendon.kakao.request.FriendtalkBuilder;
import io.sendon.kakao.response.SendFriendtalk;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public abstract class KakaoSendFriendTalk extends BaseScenario {

    public void execute(String message, boolean isAd) {
        Pageable pageable = PageRequest.of(0, PHONE_PAGE_SIZE);
        Page<String> phonePage;

        do {
            phonePage = findTargets(pageable); // 추상 메서드로 대상 가져오기
            if (!phonePage.getContent().isEmpty()) {
                try {
                    SendFriendtalk friendTalk = sendon.kakao.sendFriendtalk(new FriendtalkBuilder()
                            .setProfileId(KKO_CHANNEL_ID) // TODO: 채널 등록하기
                            .setTo(phonePage.getContent())
                            .setMessage(message)
                            .setIsAd(isAd)
                    );
                    log.info("응답: {}", gson.toJson(friendTalk));
                } catch (Exception e) {
                    log.error("친구톡 발송 중 오류 발생. Page: {}, Size: {}", phonePage.getNumber(), phonePage.getSize(), e);
                }
            }
            pageable = phonePage.nextPageable();
        } while (phonePage.hasNext());
    }

    @Override
    public String getDescription() {
        return "[카카오] 친구톡 발송";
    }

    protected abstract Page<String> findTargets(Pageable pageable); // 달라지는 부분만 서브클래스에서 구현

    @Async
    public void sendInfo(String message) { // 광고 아님
        execute(message, false);
    }

    @Async
    public void sendAd(String message) { // 광고
        execute(message, true);
    }

}