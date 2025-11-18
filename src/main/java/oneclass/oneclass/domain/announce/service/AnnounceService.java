package oneclass.oneclass.domain.announce.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.domain.announce.entity.Announce;
import oneclass.oneclass.domain.announce.error.AnnounceError;
import oneclass.oneclass.domain.announce.repository.AnnounceRepository;
import oneclass.oneclass.domain.sendon.event.AnnounceSavedEvent;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AnnounceService {
    private final AnnounceRepository announceRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AnnounceResponse createAnnounce(CreateAnnounceRequest request) {

        Announce announce = Announce.builder()
                .title(request.title())
                .content(request.content())
                // 중요한 공지인지 아닌지 체크가 가능한 Boolean 속성의 important
                .important(request.important())
                .build();

        // 만약 메세지 발송 코드가 저장 코드 위에 있을 경우, 저장에 실패했지만 메세지는 전송되는 경우가 있을 수 있음
        Announce savedAnnounce = announceRepository.save(announce);

        // Announce가 저장되면 이벤트 발생시킴
        eventPublisher.publishEvent(new AnnounceSavedEvent(request.content(), request.title()));

        return AnnounceResponse.of(savedAnnounce);
    }

    @Transactional
    public AnnounceResponse createAnnounceForLesson(CreateAnnounceRequest request, Long lessonId) {

        Announce announce = Announce.builder()
                .title(request.title())
                .content(request.content())
                .important(request.important())
                .lessonId(lessonId) // ✅ 반 ID 저장
                .build();

        Announce saved = announceRepository.save(announce);

        // 반별 공지이므로 반 학생에게만 알림을 보낼 수 있음
        eventPublisher.publishEvent(
                new AnnounceSavedEvent(
                        request.content(),
                        "[반공지] " + request.title()
                )
        );

        return AnnounceResponse.of(saved);
    }

    public List<AnnounceResponse> findAnnouncesByLessonId(Long lessonId) {
        return announceRepository.findByLessonId(lessonId)
                .stream()
                .map(AnnounceResponse::of)
                .toList();
    }



    @Transactional
    public AnnounceResponse createAnnounceForMember(CreateAnnounceRequest request, Long memberId) {
        Announce announce = Announce.builder()
                .title(request.title())
                .content(request.content())
                .important(request.important())
                .memberId(memberId)
                .build();

        Announce saved = announceRepository.save(announce);

        // (선택) 학생 개인에게 알림 이벤트 발송
        return AnnounceResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnounceResponse> findAnnouncesByMemberId(Long memberId) {
        return announceRepository.findByMemberId(memberId)
                .stream()
                .map(AnnounceResponse::of)
                .toList();
    }

    public AnnounceResponse findAnnounceById(Long id) {
        Announce announce = announceRepository.findById(id)
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        return AnnounceResponse.of(announce);
    }

    public List<AnnounceResponse> findAnnouncesByTitle(String title) {
        return announceRepository.findByTitle(title)
                .stream()
                .map(AnnounceResponse::of)
                .toList();
    }

    @Transactional
    public AnnounceResponse updateAnnounce(UpdateAnnounceRequest request) {
        Announce announce = announceRepository.findById(request.id())
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        // Use entity's update helper if available to avoid relying on individual setters
        // (keeps encapsulation and works even if there are no setTitle/setContent methods)
        announce.update(request.title(), request.content(), request.important());

        // record membership/targeting information (use correct record accessor name)
        if (request.memberId() != null) {
            announce.setMemberId(request.memberId());
        }


        return AnnounceResponse.of(announce);
    }

    @Transactional
    public void deleteAnnounce(Long id) {
        Announce announce = announceRepository.findById(id)
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        announceRepository.delete(announce);
    }

    public List<AnnounceResponse> findAll() {
        return announceRepository.findAll().stream().map(AnnounceResponse::of).collect(Collectors.toList());
    }
}