package oneclass.oneclass.domain.sendon.sms.longmessage;

import java.util.List;

import org.springframework.stereotype.Component;

import io.sendon.sms.response.SendSms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.global.exception.CustomException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSendLongMessageToMemberNow extends BaseScenario {

    private final MemberRepository memberRepository;

    public void sendToMember(String message, String title, Long memberId) {
        if (memberId == null) {
            throw new CustomException(MemberError.INVALID_LESSON_ID_VALUE);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        String phone = member.getPhone();
        
        if (phone == null || phone.isEmpty()) {
            log.warn("회원의 전화번호가 없어 메시지를 발송할 수 없습니다. memberId: {}", memberId);
            return;
        }

        try {
            SendSms sendSms = sendon.sms.sendLms(
                    SMS_MOBILE_FROM,
                    List.of(phone),
                    title,
                    message,
                    false,
                    null
            );
            log.debug("개인 LMS 발송 응답: {}", gson.toJson(sendSms));
            log.info("개인 LMS 발송 완료. memberId: {}, phone: {}", memberId, phone);
        } catch (Exception e) {
            log.error("개인 LMS 메시지 발송 중 오류 발생. memberId: {}, phone: {}", memberId, phone, e);
        }
    }

    @Override
    public String getDescription() {
        return "[LMS] 학생 개인 즉시문자 발송";
    }
}
