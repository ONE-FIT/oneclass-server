package oneclass.oneclass.domain.sendon.event;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToAll;
import oneclass.oneclass.domain.sendon.kakao.message.KakaoSendFriendTalkToTarget;
import oneclass.oneclass.domain.sendon.sms.longmessage.SmsSendLongMessageToAllNow;
import oneclass.oneclass.domain.sendon.sms.shortmessage.SmsSendShortMessageNow;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EntitySavedEventListener {
    private final SmsSendLongMessageToAllNow smsSendLongMessageToAllNow;
    private final SmsSendLongMessageToLessonNow smsSendLongMessageToLessonNow;
    private final SmsSendLongMessageToMemberNow smsSendLongMessageToMemberNow;
    private final SmsSendShortMessageNow smsSendShortMessageNow;
    private final SmsResetPasswordCode smsResetPasswordCode;
    private final SmsSendLongMessageSchedule smsSendLongMessageSchedule;

    // 메세지 발송은 비동기 처리

    // 전체 공지가 생성되면 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceSavedEvent(AnnounceSavedEvent event) {
        smsSendLongMessageToAllNow.send(event.content(), event.title());
    }

    // 반별 공지가 생성되면 해당 반 학생들에게 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceForLessonSavedEvent(AnnounceForLessonSavedEvent event) {
        // Thread-safe: lessonId를 파라미터로 전달
        smsSendLongMessageToLessonNow.sendToLesson(event.content(), event.title(), event.lessonId());
    }

    // 학생 개인 공지가 생성되면 해당 학생에게 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnnounceForMemberSavedEvent(AnnounceForMemberSavedEvent event) {
        // Thread-safe: memberId를 파라미터로 전달
        smsSendLongMessageToMemberNow.sendToMember(event.content(), event.title(), event.memberId());
    }

    // 과제가 저장되면 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssignmentSavedEvent(TaskAssignmentSavedEvent event) {
        smsSendShortMessageNow.send(event.description(), event.title(), event.memberId());
    }

    // 예약 공지가 저장되면 메세지 발송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVerificationCodeSavedEvent(VerificationCodeSavedEvent event) {
        smsResetPasswordCode.send("비밀번호 재설정 코드 : " + event.tempCode(), event.phone());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationSavedEvent(ReservationAnnounceSavedEvent event) {
        smsSendLongMessageSchedule.send(event.content(), event.title(), event.reservation());
    }
}