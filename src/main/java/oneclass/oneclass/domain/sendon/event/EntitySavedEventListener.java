package oneclass.oneclass.domain.sendon.event;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToAll;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToOne;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EntitySavedEventListener {
    private final KakaoSendFriendTalkToAll kakaoSendFriendTalkToAll;
    private final KakaoSendFriendTalkToOne kakaoSendFriendTalkToOne;

    // 공지가 생성되면 메세지 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceSavedEvent(AnnounceSavedEvent event) {
        kakaoSendFriendTalkToAll.sendInfo(event.content());
    }

    // 과제가 저장되면 메세지 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssignmentSavedEvent(TaskAssignmentSavedEvent event) {

        // TODO: TaskAssignment가 저장되는 대상에게 메세지를 보내세요. 만약 memberId를 넘겨주지 않는다면, 로직을 수정하세요.
    }
}