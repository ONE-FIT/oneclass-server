package oneclass.oneclass.domain.sendon.sms.longmessage;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Deprecated
@Component
@RequiredArgsConstructor
public class SmsSendLongMessageToAllNow extends AbstractLongMessage {

    private final MemberRepository memberRepository;

    @Override
    protected Page<String> findTargets(Pageable pageable) {
        return memberRepository.findAllPhones(pageable);
    }

    @Override
    public String getDescription() {
        return "[LMS] 즉시문자 발송";
    }
}