package oneclass.oneclass.domain.sendon.kakao.message;

import io.sendon.kakao.request.FriendtalkBuilder;
import io.sendon.kakao.response.SendFriendtalk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.global.auth.member.error.UserError;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoSendFriendTalkToOne extends BaseScenario {

    private final MemberRepository memberRepository;

    public void execute(String message, boolean isAd, Long memberId) {

        try {
            SendFriendtalk friendTalk = sendon.kakao.sendFriendtalk(new FriendtalkBuilder()
                    .setProfileId(KKO_CHANNEL_ID) // TODO: 채널 등록하기
                    .setTo(Arrays.asList(memberRepository.findById(memberId).
                            orElseThrow(() -> new CustomException(UserError.NOT_FOUND))
                            .getPhone()
                    ))
                    .setMessage(message)
                    .setIsAd(isAd)
            );
            log.info("응답: {}", gson.toJson(friendTalk));
        } catch (Exception e) {
            log.error("친구톡 발송 중 오류 발생", e);
        }

    }

    @Override
    public String getDescription() {
        return "[카카오] 친구톡 발송";
    }

    @Async
    public void sendInfo(String message, Long memberId) { // 광고 아님
        execute(message, false, memberId);
    }

    @Async
    public void sendAd(String message, Long memberId) { // 광고
        execute(message, true, memberId);
    }

}