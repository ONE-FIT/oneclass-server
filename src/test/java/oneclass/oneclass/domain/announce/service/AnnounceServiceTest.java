package oneclass.oneclass.domain.announce.service;

import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.entity.Announce;
import oneclass.oneclass.domain.announce.repository.AnnounceRepository;
import oneclass.oneclass.domain.sendon.event.AnnounceSavedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.security.Principal;

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

        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("teacher01");
        CreateAnnounceRequest request = new CreateAnnounceRequest(
                "공지 제목",
                "공지 내용",
                true,
                "2025-10-29 18:00:00"
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
        announceService.createAnnounce(mockPrincipal, request);

        // then
        Mockito.verify(eventPublisher).publishEvent(any(AnnounceSavedEvent.class));
    }
}
