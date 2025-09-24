package oneclass.oneclass.domain.sendon.kakao.message;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoSendFriendTalkToTarget extends KakaoSendFriendTalk{

    private final MemberRepository memberRepository;

    // TODO: 일부 학생에게만 메세지가 전송되도록 구현하세요
    @Override
    protected Page<String> findTargets(Pageable pageable) {
        return null;
    }
}