package oneclass.oneclass.domain.sendon.sms.longmessage;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Deprecated
@Component
@RequiredArgsConstructor
public class SmsSendLongMessageToTargetNow extends AbstractLongMessage {

    private final MemberRepository memberRepository;

    // TODO: 메세지가 보내지는 인원이 선택되도록 구현하세요.
    @Override
    protected Page<String> findTargets(Pageable pageable) {
        return memberRepository.findAllPhones(pageable);
    }

    @Override
    public String getDescription() {
        return "[LMS] 즉시문자 발송";
    }
}