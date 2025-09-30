package oneclass.oneclass.domain.announce.service;

import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.entity.Announce;
import oneclass.oneclass.domain.sendon.event.AnnounceSavedEvent;
import oneclass.oneclass.domain.announce.repository.AnnounceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AnnounceServiceTest {

    @Mock
    AnnounceRepository announceRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    AnnounceService announceService;

    @Test
    void createAnnounce_publishesEvent() {
        // given
        CreateAnnounceRequest request = new CreateAnnounceRequest(
                "공지 제목",
                "공지 내용",
                true
        );

        Announce savedAnnounce = Announce.builder()
                .id(1L)
                .title(request.title())
                .content(request.content())
                .important(request.important())
                .build();

        // save 호출 시 savedAnnounce 반환하도록 설정
        Mockito.when(announceRepository.save(any(Announce.class))).thenReturn(savedAnnounce);

        // when
        announceService.createAnnounce(request);

        // then
        Mockito.verify(eventPublisher).publishEvent(any(AnnounceSavedEvent.class));
    }
}
