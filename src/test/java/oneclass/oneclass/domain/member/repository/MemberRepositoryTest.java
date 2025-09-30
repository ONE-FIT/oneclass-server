package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private Member member1, member2, member3;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(Member.builder()
                .username("alice")
                .name("Alice")
                .role(Role.STUDENT)
                .build());

        member2 = memberRepository.save(Member.builder()
                .username("bob")
                .name("Bob")
                .role(Role.STUDENT)
                .build());

        member3 = memberRepository.save(Member.builder()
                .username("charlie")
                .name("Charlie")
                .role(Role.STUDENT)
                .build());

        // member1은 출석 처리
        attendanceRepository.save(Attendance.builder()
                .member(member1)
                .attendanceStatus(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .checkOutTime(LocalDateTime.now().plusHours(1))
                .date(LocalDate.now())
                .build());
    }

    @Test
    void findAbsentMembers() {
        LocalDate today = LocalDate.now();

        List<Member> absentMembers = memberRepository.findAbsentMembers(today);

        // member1은 PRESENT라서 제외, member2, member3만 반환
        assertThat(absentMembers).containsExactlyInAnyOrder(member2, member3);
        assertThat(absentMembers).doesNotContain(member1);
    }
}
