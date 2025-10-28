package oneclass.oneclass.domain.sendon.kakao.message;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Deprecated
@Component
@RequiredArgsConstructor
public class KakaoSendFriendTalkToAll extends KakaoSendFriendTalk{

    private final MemberRepository memberRepository;

    @Override
    protected Page<String> findTargets(Pageable pageable) {
        return memberRepository.findAllPhones(pageable);
    }
}