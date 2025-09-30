package oneclass.oneclass.domain.attendance.service;

import oneclass.oneclass.domain.attendance.dto.response.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.Attendance;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.repository.AttendanceRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.entity.Role;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GetTodayAbsentMembersTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminAttendanceService adminAttendanceService;

    @Test
    void getTodayAbsentMembers_returnsAttendanceResponses() {
        LocalDate today = LocalDate.now();

        // 여러 명 Member 생성
        Member member1 = Member.builder()
                .id(1L)
                .username("alice")
                .name("Alice")
                .role(Role.STUDENT)
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .username("bob")
                .name("Bob")
                .role(Role.STUDENT)
                .build();

        Member member3 = Member.builder()
                .id(3L)
                .username("charlie")
                .name("Charlie")
                .role(Role.STUDENT)
                .build();

        // memberRepository.findAbsentMembers(today) 호출 시 반환
        Mockito.when(memberRepository.findAbsentMembers(today))
                .thenReturn(List.of(member1, member2, member3));

        // Service 호출
        List<AttendanceResponse> absentResponses = adminAttendanceService.getTodayAbsentMembers(today);

        // 검증
        assertThat(absentResponses).hasSize(3);

        assertThat(absentResponses.get(0).getName()).isEqualTo("Alice");
        assertThat(absentResponses.get(1).getName()).isEqualTo("Bob");
        assertThat(absentResponses.get(2).getName()).isEqualTo("Charlie");

        absentResponses.forEach(response ->
                assertThat(response.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT)
        );

        absentResponses.forEach(response ->
                assertThat(response.getDate()).isEqualTo(today)
        );
    }

    @Test
    void getTodayAbsentMembers_returnsAttendanceResponses_exceptPresent() {
        LocalDate today = LocalDate.now();

        // 여러 명 Member 생성
        Member member1 = Member.builder()
                .id(1L)
                .username("alice")
                .name("Alice")
                .role(Role.STUDENT)
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .username("bob")
                .name("Bob")
                .role(Role.STUDENT)
                .build();

        Member member3 = Member.builder()
                .id(3L)
                .username("charlie")
                .name("Charlie")
                .role(Role.STUDENT)
                .build();

        Attendance attendance1 = Attendance.builder()
                .attendanceStatus(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .checkOutTime(LocalDateTime.now().plusMinutes(60))
                .date(LocalDate.now())
                .member(member1).build();

        // memberRepository.findAbsentMembers(today) 호출 시 반환
        Mockito.when(memberRepository.findAbsentMembers(today))
                .thenReturn(List.of(member2, member3));

        // Service 호출
        List<AttendanceResponse> absentResponses = adminAttendanceService.getTodayAbsentMembers(today);

        // 검증
        assertThat(absentResponses).hasSize(2);

        assertThat(absentResponses.get(0).getName()).isEqualTo("Bob");
        assertThat(absentResponses.get(1).getName()).isEqualTo("Charlie");

        absentResponses.forEach(response ->
                assertThat(response.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT)
        );

        absentResponses.forEach(response ->
                assertThat(response.getDate()).isEqualTo(today)
        );
    }
}

