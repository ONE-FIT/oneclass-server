package oneclass.oneclass.domain.sendon.event;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToAll;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToTarget;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EntitySavedEventListener {
    private final KakaoSendFriendTalkToAll kakaoSendFriendTalkToAll;
    private final KakaoSendFriendTalkToTarget kakaoSendFriendTalkToTarget;

    // 공지가 생성되면 메세지 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceSavedEvent(AnnounceSavedEvent event) {
        kakaoSendFriendTalkToAll.sendInfo(event.content());
    }

    // 과제가 저장되면 메세지 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssignmentSavedEvent(TaskAssignmentSavedEvent event) {

        kakaoSendFriendTalkToTarget.sendInfo(event.description(), event.memberId());
    }
}