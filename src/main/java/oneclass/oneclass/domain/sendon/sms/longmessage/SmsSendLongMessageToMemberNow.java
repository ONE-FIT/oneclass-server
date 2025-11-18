package oneclass.oneclass.domain.sendon.sms.longmessage;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SmsSendLongMessageToMemberNow extends AbstractLongMessage {

    private final MemberRepository memberRepository;

    @Setter
    private Long memberId;

    @Override
    protected Page<String> findTargets(Pageable pageable) {
        if (memberId == null) {
            throw new CustomException(MemberError.INVALID_LESSON_ID_VALUE);
        }

        String phone = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."))
                .getPhone();

        if (phone == null || phone.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 단일 학생에게만 발송하므로 첫 페이지만 전화번호 포함
        if (pageable.getPageNumber() == 0) {
            return new PageImpl<>(List.of(phone), pageable, 1);
        } else {
            return new PageImpl<>(List.of(), pageable, 1);
        }
    }

    @Override
    public String getDescription() {
        return "[LMS] 학생 개인 즉시문자 발송";
    }
}
