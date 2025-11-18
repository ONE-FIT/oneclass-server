package oneclass.oneclass.domain.sendon.sms.longmessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;

import io.sendon.Sendon;
import io.sendon.sms.response.SendSms;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.global.exception.CustomException;

@Slf4j
@Component
public class SmsSendLongMessageToLessonNow extends BaseScenario {
    
    private final MemberRepository memberRepository;

    public SmsSendLongMessageToLessonNow(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.sendon = Sendon.getInstance(USER_ID, USER_APIKEY, true);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    public void sendToLesson(String message, String title, Long lessonId) {
        if (lessonId == null) {
            throw new CustomException(LessonError.INVALID_LESSON_ID_VALUE);
        }

        Pageable pageable = PageRequest.of(0, PHONE_PAGE_SIZE);
        Page<String> phonePage;

        do {
            // DB 레벨에서 페이징 처리 - 메모리 효율적
            phonePage = memberRepository.findPhonesByLessonId(lessonId, pageable);
            
            if (!phonePage.getContent().isEmpty()) {
                try {
                    SendSms sendSms = sendon.sms.sendLms(
                            SMS_MOBILE_FROM,
                            phonePage.getContent(),
                            title,
                            message,
                            false,
                            null
                    );
                    log.debug("반별 LMS 발송 응답: {}", gson.toJson(sendSms));
                } catch (Exception e) {
                    log.error("반별 LMS 메시지 발송 중 오류 발생. lessonId: {}, Page: {}, Size: {}", 
                            lessonId, phonePage.getNumber(), phonePage.getSize(), e);
                }
            }
            
            pageable = phonePage.nextPageable();
        } while (phonePage.hasNext());
        
        log.info("반별 LMS 발송 완료. lessonId: {}, 총 페이지: {}", lessonId, phonePage.getTotalPages());
    }

    public String getDescription() {
        return "[LMS] 반별 즉시문자 발송";
    }
}
