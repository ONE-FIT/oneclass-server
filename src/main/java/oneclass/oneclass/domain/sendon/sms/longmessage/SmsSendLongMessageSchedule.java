package oneclass.oneclass.domain.sendon.sms.longmessage;

import io.sendon.sms.request.MmsBuilder;
import io.sendon.sms.request.Reservation;
import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Deprecated
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSendLongMessageSchedule extends BaseScenario {

    private final MemberRepository memberRepository;

    public void execute(String message, String title, String reservationTime) {
        Reservation reservation = new Reservation(reservationTime); // 동적으로 받은 예약 시간
        Pageable pageable = PageRequest.of(0, PHONE_PAGE_SIZE);
        Page<String> phonePage;

        do {
            phonePage = findTargets(pageable);
            if (!phonePage.getContent().isEmpty()) {
                try {
                    SendSms sendSms = sendon.sms.sendMms(new MmsBuilder()
                            .setFrom(SMS_MOBILE_FROM)
                            .setTo(phonePage.getContent())
                            .setTitle(title)
                            .setMessage(message)
                            .setReservation(reservation)
                            .setIsAd(false)
                    );
                    log.info("예약 LMS 발송 완료. Page: {}, Count: {}, Reservation: {}",
                            phonePage.getNumber(), phonePage.getNumberOfElements(), reservationTime);
                    log.debug("응답: {}", gson.toJson(sendSms));
                } catch (Exception e) {
                    log.error("예약 LMS 발송 중 오류 발생. Page: {}, Size: {}, Reservation: {}",
                            phonePage.getNumber(), phonePage.getSize(), reservationTime, e);
                }
            }
            pageable = phonePage.nextPageable();
        } while (phonePage.hasNext());
    }

    protected Page<String> findTargets(Pageable pageable) {
        return memberRepository.findAllPhones(pageable);
    }

    @Async
    public void send(String message, String title, String reservationTime) {
        execute(message, title, reservationTime);
    }

    public String getDescription() {
        return "[LMS] 예약문자 발송";
    }
}