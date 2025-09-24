package oneclass.oneclass.domain.sendon.sms.event;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToAll;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EntitySavedEventListener {
    private final KakaoSendFriendTalkToAll kakaoSendFriendTalkToAll;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceSavedEvent(AnnounceSavedEvent event) {
        kakaoSendFriendTalkToAll.sendInfo(event.getContent());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskSavedEvent(TaskSavedEvent event) {

        // TODO: 과제 대상에게만 메세지를 보내세요.
        // 현재 모든 사용자에게 메세지를 보냅니다.
        kakaoSendFriendTalkToAll.sendInfo(event.getDescription());
    }
}