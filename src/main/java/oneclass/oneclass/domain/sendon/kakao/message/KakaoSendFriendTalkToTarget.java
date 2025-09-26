package oneclass.oneclass.domain.sendon.kakao.message;

import io.sendon.kakao.request.FriendtalkBuilder;
import io.sendon.kakao.response.SendFriendtalk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oneclass.oneclass.domain.sendon.BaseScenario;
import oneclass.oneclass.global.auth.member.error.MemberError;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoSendFriendTalkToTarget extends BaseScenario {

    private final MemberRepository memberRepository;

    public void execute(String message, boolean isAd, List<Long> memberIds) {
        try {
            // memberId 리스트로 phone 번호 추출
            List<String> phones = memberIds.stream()
                    .map(id -> memberRepository.findById(id)
                            .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND))
                            .getPhone()
                    )
                    .collect(Collectors.toList());

            SendFriendtalk friendTalk = sendon.kakao.sendFriendtalk(new FriendtalkBuilder()
                    .setProfileId(KKO_CHANNEL_ID) // TODO: 채널 등록하기
                    .setTo(phones) // 여러 명에게 발송
                    .setMessage(message)
                    .setIsAd(isAd)
            );

            log.info("친구톡 발송 대상: {}", phones);
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
    public void sendInfo(String message, List<Long> memberIds) { // 광고 아님
        execute(message, false, memberIds);
    }

    @Async
    public void sendAd(String message, List<Long> memberIds) { // 광고
        execute(message, true, memberIds);
    }

}
