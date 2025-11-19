package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MemberRepository
 * - 기본 단건/일괄 조회
 * - 출석/수업 관련 편의 쿼리
 * - 교사-학생/부모-학생 연관 fetch 쿼리
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m.phone FROM Member m WHERE m.lesson.lessonId = :lessonId AND m.phone IS NOT NULL AND m.phone != ''")
    Page<String> findPhonesByLessonId(@Param("lessonId") Long lessonId, Pageable pageable);

    // 단건 조회
    Optional<Member> findByUsername(String username);
    Optional<Member> findByPhone(String phone);


    // 다건 조회(전화번호 리스트로)
    List<Member> findAllByPhoneIn(Collection<String> phones);

    @Query("select m.phone from Member m where m.role in (oneclass.oneclass.domain.member.entity.Role.STUDENT, oneclass.oneclass.domain.member.entity.Role.PARENT)")
    Page<String> findAllPhones(Pageable pageable);

    // 특정 학생 id 집합으로 전화번호 페이징
    @Query("select m.phone from Member m where m.id in :studentIds")
    Page<String> findPhonesByIds(@Param("studentIds") List<Long> studentIds, Pageable pageable);

    /**
     * 수업별 & 날짜별 '결석' 대상 학생 목록
     * 조건: 해당 lesson 의 students 중 그 날짜에 PRESENT/LATE/EXCUSED 기록이 없는 사람
     */
    @Query("""
        select m
        from Member m
        where m in (
            select s from Lesson l
            join l.students s
            where l.lessonId = :lessonId
        )
        and not exists (
            select 1 from Attendance a
            where a.member.id = m.id
              and a.date = :date
              and a.attendanceStatus in ('PRESENT','LATE','EXCUSED')
        )
    """)
    List<Member> findAbsentMembersByLessonAndDate(@Param("lessonId") Long lessonId,
                                                  @Param("date") LocalDate date);

    List<Member> findAllByUsernameIn(Collection<String> usernames);
}