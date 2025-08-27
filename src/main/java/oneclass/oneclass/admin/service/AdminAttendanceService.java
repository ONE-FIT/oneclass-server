package oneclass.oneclass.admin.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.attendance.entity.AttendanceEntity;
import oneclass.oneclass.attendance.repository.AttendanceRepository;
import oneclass.oneclass.auth.entity.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final AttendanceRepository attendanceRepository;

    // 오늘 출석한 사람
    public List<Member> getTodayPresentMembers() {
        List<AttendanceEntity> todayRecords = attendanceRepository.findByDateAndPresent(LocalDate.now(), true);
        return todayRecords.stream()
                .map(AttendanceEntity::getMember)
                .collect(Collectors.toList());
    }

    // 오늘 출석하지 않은 사람
    public List<Member> getTodayAbsentMembers(List<Member> allMembers) {
        List<Member> presentMembers = getTodayPresentMembers();
        return allMembers.stream()
                .filter(m -> !presentMembers.contains(m))
                .collect(Collectors.toList());
    }

    // 전체 출석 정보
    public List<AttendanceEntity> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    // 특정 학생 출석 기록
    public List<AttendanceEntity> getAttendanceByMember(Member member) {
        return attendanceRepository.findByMember(member);
    }
}
