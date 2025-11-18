package oneclass.oneclass.domain.sendon.event;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.sms.longmessage.SmsSendLongMessageToAllNow;
import oneclass.oneclass.domain.sendon.sms.shortmessage.SmsResetPasswordCode;
import oneclass.oneclass.domain.sendon.sms.shortmessage.SmsSendShortMessageNow;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EntitySavedEventListener {
    private final SmsSendLongMessageToAllNow smsSendLongMessageToAllNow;
    private final SmsSendShortMessageNow smsSendShortMessageNow;
    private final SmsResetPasswordCode smsResetPasswordCode;

    // 공지가 생성되면 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceSavedEvent(AnnounceSavedEvent event) {
        smsSendLongMessageToAllNow.send(event.content(), event.title());
    }

    // 과제가 저장되면 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssignmentSavedEvent(TaskAssignmentSavedEvent event) {
        smsSendShortMessageNow.send(event.description(), event.title(), event.memberId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVerificationCodeSavedEvent(VerificationCodeSavedEvent event) {
        smsResetPasswordCode.send("비밀번호 재설정 코드 : " + event.tempCode(), event.phone());
    }

}