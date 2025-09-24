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

    public AnnounceResponse findAnnounceById(Long id) {
        Announce announce = announceRepository.findById(id)
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        return AnnounceResponse.of(announce);
    }

    public AnnounceResponse findAnnounceByTitle(String title) {
        Announce announce = announceRepository.findByTitle(title)
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        return AnnounceResponse.of(announce);
    }

    public AnnounceResponse updateAnnounce(UpdateAnnounceRequest request) {
        Announce announce = announceRepository.findById(request.id())
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        announce.setTitle(request.title());
        announce.setContent(request.content());
        announce.setImportant(request.important());

        return AnnounceResponse.of(announceRepository.save(announce));
    }

    public void deleteAnnounce(Long id) {
        Announce announce = announceRepository.findById(id)
                .orElseThrow(() -> new CustomException(AnnounceError.NOT_FOUND));
        announceRepository.delete(announce);
    }

    public List<AnnounceResponse> findAll() {
        return announceRepository.findAll().stream().map(AnnounceResponse::of).collect(Collectors.toList());
    }
}