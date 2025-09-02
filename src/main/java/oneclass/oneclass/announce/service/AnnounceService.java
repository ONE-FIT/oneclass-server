package oneclass.oneclass.announce.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.announce.entity.Announce;
import oneclass.oneclass.announce.repository.AnnounceRepository;
import oneclass.oneclass.task.dto.response.TaskResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AnnounceService {
    private final AnnounceRepository announceRepository;

    public AnnounceResponse createAnnounce(CreateAnnounceRequest request) {
        Announce announce = Announce.builder()
                .title(request.title())
                .content(request.content())
                .important(request.important())
                .build();
        return AnnounceResponse.of(announceRepository.save(announce));
    }

    public AnnounceResponse findAnnounceById(Long id) {
        Announce announce = announceRepository.findById(id).orElse(null);
        if (announce == null) {
            throw new IllegalArgumentException(id+"는 존재하지 않는 공지입니다");
        }
        return AnnounceResponse.of(announce);
    }

    public AnnounceResponse updateAnnounce(UpdateAnnounceRequest request) {
        Announce announce = announceRepository.findById(request.id()).orElse(null);
        if (announce == null) {
            throw new IllegalArgumentException(request.id()+"는 없는 공지입니다.");
        }
        announce.setTitle(request.title());
        announce.setContent(request.content());
        announce.setImportant(request.important());

        return AnnounceResponse.of(announceRepository.save(announce));
    }

    public void deleteAnnounce(Long id) {
        Announce announce = announceRepository.findById(id).orElse(null);
        if (announce == null) {
            throw new IllegalArgumentException(id+"는 없는 공지입니다");
        }
        announceRepository.delete(announce);
    }

    public List<AnnounceResponse> findAll() {
        return announceRepository.findAll().stream().map(AnnounceResponse::of).collect(Collectors.toList());
    }
}
