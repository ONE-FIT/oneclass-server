package oneclass.oneclass.domain.announce.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.event.AnnounceCreatedEvent;
import oneclass.oneclass.domain.message.sms.longmessage.SmsSendLongMessageNow;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AnnounceEventListener {
    private final SmsSendLongMessageNow smsSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceCreatedEvent(AnnounceCreatedEvent event) {
        smsSender.send(event.getContent(), event.getTitle());
    }
}